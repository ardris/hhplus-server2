package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.QueueTokenException;
import kr.hhplus.be.server.exception.ReservationException;
import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.model.Reservation;
import kr.hhplus.be.server.model.Seat;
import kr.hhplus.be.server.repository.ConcertRepository;
import kr.hhplus.be.server.repository.ReservationRepository;
import kr.hhplus.be.server.repository.SeatRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 예약 관리 서비스 구현체
 */
public class ReservationServiceImpl implements ReservationService {
    

    // 5분동안 설정
    private static final int HOLD_DURATION_MINUTES = 5;
    

    //예약정보 인메모리 저장소 사용
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;
    private final QueueService queueService;
    
    // 좌석별 락 (동시성 제어)
    private final java.util.concurrent.ConcurrentHashMap<String, ReentrantLock> seatLocks = new java.util.concurrent.ConcurrentHashMap<>();
    
    public ReservationServiceImpl(ReservationRepository reservationRepository, 
                                 SeatRepository seatRepository,
                                 ConcertRepository concertRepository,
                                 QueueService queueService) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
        this.queueService = queueService;
    }
    
    @Override
    // 좌석 임시 배정 (5분)
    public Reservation holdSeat(String tokenId, String concertDate, String seatId, BigDecimal ticketPrice) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자가 활성 상태인지 확인
        if (!queueService.isUserActive(userId)) {
            throw new QueueTokenException.NotActiveInQueueException(0);
        }
        
        // 입력값 검증
        if (concertDate == null || concertDate.trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 날짜는 필수입니다.");
        }
        if (seatId == null || seatId.trim().isEmpty()) {
            throw new IllegalArgumentException("좌석 ID는 필수입니다.");
        }
        if (ticketPrice == null || ticketPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("티켓 가격은 0보다 커야 합니다.");
        }
        
        // 좌석 락 획득
        String seatLockKey = concertDate + "#" + seatId;
        ReentrantLock seatLock = seatLocks.computeIfAbsent(seatLockKey, k -> new ReentrantLock());
        
        seatLock.lock();
        try {
        // 좌석 상태 확인
        Optional<Seat> seatOptional = seatRepository.findByConcertDateAndSeatId(concertDate, seatId);
        if (!seatOptional.isPresent()) {
            throw new ReservationException("존재하지 않는 좌석입니다.");
        }
        Seat seat = seatOptional.get();
            
            if (!seat.isAvailable()) {
                if (seat.isHeldByUser(userId)) {
                    // 이미 해당 사용자가 임시 배정한 좌석인 경우 기존 예약 반환
                    List<Reservation> existingReservations = reservationRepository.findByConcertDateAndSeatId(concertDate, seatId);
                    for (Reservation reservation : existingReservations) {
                        if (reservation.getUserId().equals(userId) && reservation.isHold()) {
                            return reservation;
                        }
                    }
                    throw new ReservationException("예약 정보를 찾을 수 없습니다.");
                } else {
                    throw new ReservationException.SeatAlreadyHeldException();
                }
            }
            
            // 예약 생성
            String reservationId = UUID.randomUUID().toString();
            Reservation reservation = new Reservation(reservationId, userId, concertDate, seatId, ticketPrice);
            reservationRepository.save(reservation);
            
            // 좌석 임시 배정
            seat.holdSeat(userId, HOLD_DURATION_MINUTES);
            seatRepository.update(seat);
            
            return reservation;
            
        } finally {
            seatLock.unlock();
        }
    }
    
    @Override
    // 좌석 임시 배정 (콘서트 제목 포함)
    public Reservation holdSeat(String tokenId, String concertDate, String concertTitle, String seatId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자가 활성 상태인지 확인
        if (!queueService.isUserActive(userId)) {
            throw new QueueTokenException.NotActiveInQueueException(0);
        }
        
        // 콘서트 존재 여부 및 가격 조회
        Concert concert = concertRepository.findByDateAndTitle(concertDate, concertTitle);
        if (concert == null) {
            throw new ReservationException("해당 날짜와 제목의 콘서트를 찾을 수 없습니다.");
        }
        
        // 좌석 존재 여부 확인
        Seat seat = seatRepository.findByConcertDateAndSeatId(concertDate, seatId);
        if (seat == null) {
            throw new ReservationException.SeatNotFoundException();
        }
        
        // 좌석별 락 획득 (동시성 제어)
        ReentrantLock seatLock = seatLocks.computeIfAbsent(seatId, k -> new ReentrantLock());
        seatLock.lock();
        
        try {
            // 좌석 상태 확인
            if (!seat.isAvailable()) {
                if (seat.isHeldByUser(userId)) {
                    // 이미 해당 사용자가 임시 배정한 좌석인 경우 기존 예약 출력
                    List<Reservation> existingReservations = reservationRepository.findByConcertDateAndSeatId(concertDate, seatId);
                    for (Reservation reservation : existingReservations) {
                        if (reservation.getUserId().equals(userId) && reservation.isHold()) {
                            return reservation;
                        }
                    }
                    throw new ReservationException("예약 정보를 찾을 수 없습니다.");
                } else {
                    throw new ReservationException.SeatAlreadyHeldException();
                }
            }
            
            // 예약 생성 (콘서트 가격 사용)
            String reservationId = UUID.randomUUID().toString();
            Reservation reservation = new Reservation(reservationId, userId, concertDate, seatId, concert.getTicketPrice());
            reservationRepository.save(reservation);
            
            // 좌석 임시 배정
            seat.holdSeat(userId, HOLD_DURATION_MINUTES);
            seatRepository.update(seat);
            
            return reservation;
            
        } finally {
            seatLock.unlock();
        }
    }
    
    @Override
    // 예약 정보 조회
    public Reservation getReservation(String tokenId, String reservationId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 예약 조회
        Optional<Reservation> reservationOptional = reservationRepository.findByReservationId(reservationId);
        if (!reservationOptional.isPresent()) {
            throw new ReservationException("존재하지 않는 예약입니다.");
        }
        Reservation reservation = reservationOptional.get();
        
        // 본인 예약인지 확인
        if (!reservation.getUserId().equals(userId)) {
            throw new ReservationException("본인의 예약만 조회할 수 있습니다.");
        }
        
        return reservation;
    }
    
    @Override
    // 사용자의 모든 예약 목록 조회
    public List<Reservation> getUserReservations(String tokenId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        return reservationRepository.findByUserId(userId);
    }
    
    @Override
    // 만료된 임시 배정 정리
    public void cleanupExpiredHolds() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredHolds();
        
        for (Reservation reservation : expiredReservations) {
            String seatLockKey = reservation.getConcertDate() + "#" + reservation.getSeatId();
            ReentrantLock seatLock = seatLocks.computeIfAbsent(seatLockKey, k -> new ReentrantLock());
            
            seatLock.lock();
            try {
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
                
            } finally {
                seatLock.unlock();
            }
        }
    }
}
