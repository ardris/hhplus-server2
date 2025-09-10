package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.PaymentException;
import kr.hhplus.be.server.exception.ReservationException;
import kr.hhplus.be.server.model.Payment;
import kr.hhplus.be.server.model.Reservation;
import kr.hhplus.be.server.model.Seat;
import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.repository.PaymentRepository;
import kr.hhplus.be.server.repository.ReservationRepository;
import kr.hhplus.be.server.repository.SeatRepository;
import kr.hhplus.be.server.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 결제 관리 서비스 구현체
 */
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final QueueService queueService;
    
    // 예약별 락 (동시성 제어)
    private final java.util.concurrent.ConcurrentHashMap<String, ReentrantLock> paymentLocks = new java.util.concurrent.ConcurrentHashMap<>();
    
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                             ReservationRepository reservationRepository,
                             SeatRepository seatRepository,
                             TransactionRepository transactionRepository,
                             UserService userService,
                             QueueService queueService) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.queueService = queueService;
    }
    
    @Override
    // 예약 결제 처리
    public Payment processPayment(String tokenId, String reservationId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 입력값 검증
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("예약 ID는 필수입니다.");
        }
        
        // 결제 락 획득 (예약별로 동시 결제 방지)
        ReentrantLock paymentLock = paymentLocks.computeIfAbsent(reservationId, k -> new ReentrantLock());
        
        paymentLock.lock();
        try {
        // 예약 조회
        Optional<Reservation> reservationOptional = reservationRepository.findByReservationId(reservationId);
        if (!reservationOptional.isPresent()) {
            throw new ReservationException("존재하지 않는 예약입니다.");
        }
        Reservation reservation = reservationOptional.get();
            
            // 본인 예약인지 확인
            if (!reservation.getUserId().equals(userId)) {
                throw new ReservationException("본인의 예약만 결제할 수 있습니다.");
            }
            
            // 이미 결제 완료된 경우
            if (reservation.isPaid()) {
                Optional<Payment> completedPayment = paymentRepository.findCompletedPaymentByReservationId(reservationId);
                if (!completedPayment.isPresent()) {
                    throw new PaymentException("결제 정보를 찾을 수 없습니다.");
                }
                return completedPayment.get();
            }
            
            // 임시 배정 상태 확인
            if (!reservation.isHold()) {
                throw new ReservationException.ReservationStateException("결제 가능한 상태가 아닙니다.");
            }
            
            // 임시 배정 만료 확인
            if (reservation.isHoldExpired()) {
                // 만료된 예약 정리
                cleanupExpiredReservation(reservation);
                throw new ReservationException.HoldExpiredException("임시 배정이 만료되었습니다. 다시 예약해 주세요.");
            }
            
            // 잔액 확인
            if (!userService.hasSufficientBalance(userId, reservation.getTicketPrice())) {
                BigDecimal currentBalance = userService.getBalance(tokenId);
                throw new PaymentException.InsufficientBalanceException(currentBalance, reservation.getTicketPrice());
            }
            
            // 결제 처리
            String paymentId = UUID.randomUUID().toString();
            Payment payment = new Payment(paymentId, userId, reservationId, reservation.getTicketPrice());
            paymentRepository.save(payment);
            
            try {
                // 잔액 차감
                userService.deductBalance(userId, reservation.getTicketPrice());
                
                // 결제 완료 처리
                payment.completePayment();
                paymentRepository.update(payment);
                
                // 예약 상태 업데이트
                reservation.confirmPayment(paymentId);
                reservationRepository.update(reservation);
                
                // 좌석 상태 업데이트
                Optional<Seat> seatOptional = seatRepository.findByConcertDateAndSeatId(
                        reservation.getConcertDate(), reservation.getSeatId());
                if (seatOptional.isPresent()) {
                    Seat seat = seatOptional.get();
                    seat.confirmReservation();
                    seatRepository.update(seat);
                }
                
                // 대기열에서 사용자 제거
                queueService.removeUserFromQueue(userId);
                
                return payment;
                
            } catch (Exception e) {
                // 결제 실패 처리
                payment.failPayment(e.getMessage());
                paymentRepository.update(payment);
                throw new PaymentException("결제 처리 중 오류가 발생했습니다.", e);
            }
            
        } finally {
            paymentLock.unlock();
        }
    }
    
    @Override
    // 결제 정보 조회
    public Payment getPayment(String tokenId, String paymentId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 결제 조회
        Optional<Payment> paymentOptional = paymentRepository.findByPaymentId(paymentId);
        if (!paymentOptional.isPresent()) {
            throw new PaymentException("존재하지 않는 결제입니다.");
        }
        Payment payment = paymentOptional.get();
        
        // 본인 결제인지 확인
        if (!payment.getUserId().equals(userId)) {
            throw new PaymentException("본인의 결제만 조회할 수 있습니다.");
        }
        
        return payment;
    }
    
    @Override
    // 사용자의 모든 결제 내역 조회
    public List<Payment> getUserPayments(String tokenId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        return paymentRepository.findByUserId(userId);
    }
    
    /**
     * 만료된 예약을 정리합니다.
     */
    private void cleanupExpiredReservation(Reservation reservation) {
        // 예약 상태 업데이트
        reservation.expireHold();
        reservationRepository.update(reservation);
        
        // 좌석 상태 업데이트
        Optional<Seat> seatOptional = seatRepository.findByConcertDateAndSeatId(
                reservation.getConcertDate(), reservation.getSeatId());
        if (seatOptional.isPresent()) {
            Seat seat = seatOptional.get();
            seat.releaseHold();
            seatRepository.update(seat);
        }
    }
    
    @Override
    // 예약 결제 처리 (거래 내역 포함)
    public Payment processPaymentWithTransaction(String tokenId, String reservationId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자가 활성 상태인지 확인
        if (!queueService.isUserActive(userId)) {
            throw new PaymentException("대기열에서 활성화되지 않은 사용자입니다.");
        }
        
        // 예약 조회
        Optional<Reservation> reservationOptional = reservationRepository.findByReservationId(reservationId);
        if (reservationOptional.isEmpty()) {
            throw new ReservationException("예약을 찾을 수 없습니다.");
        }
        
        Reservation reservation = reservationOptional.get();
        
        // 예약 소유자 확인
        if (!reservation.getUserId().equals(userId)) {
            throw new PaymentException("본인의 예약만 결제할 수 있습니다.");
        }
        
        // 예약 상태 확인
        if (!reservation.isHold()) {
            throw new PaymentException("임시 배정된 예약만 결제할 수 있습니다.");
        }
        
        // 예약 만료 확인
        if (reservation.isExpired()) {
            cleanupExpiredReservation(reservation);
            throw new PaymentException("만료된 예약입니다.");
        }
        
        // 예약별 락 획득 (동시성 제어)
        ReentrantLock paymentLock = paymentLocks.computeIfAbsent(reservationId, k -> new ReentrantLock());
        paymentLock.lock();
        
        try {
            // 잔액 확인
            if (!userService.hasSufficientBalance(userId, reservation.getTicketPrice())) {
                throw new PaymentException.InsufficientBalanceException();
            }
            
            // 결제 처리
            Payment payment = new Payment(reservationId, userId, reservation.getTicketPrice());
            paymentRepository.save(payment);
            
            // 잔액 차감
            userService.deductBalance(userId, reservation.getTicketPrice());
            
            // 예약 확정
            reservation.confirmPayment(payment.getPaymentId());
            reservationRepository.update(reservation);
            
            // 좌석 예약 확정
            Optional<Seat> seatOptional = seatRepository.findByConcertDateAndSeatId(
                    reservation.getConcertDate(), reservation.getSeatId());
            if (seatOptional.isPresent()) {
                Seat seat = seatOptional.get();
                seat.reserveSeat(userId);
                seatRepository.update(seat);
            }
            
            // 결제 거래 내역 생성
            Transaction transaction = new Transaction(
                userId,
                Transaction.TransactionType.PAYMENT,
                reservation.getTicketPrice(),
                userService.getBalance(tokenId),
                "콘서트 좌석 결제 - " + reservation.getConcertDate() + " " + reservation.getSeatId() + "번 좌석"
            );
            transactionRepository.save(transaction);
            
            // 토큰 만료
            queueService.expireToken(tokenId);
            
            return payment;
            
        } finally {
            paymentLock.unlock();
        }
    }
}
