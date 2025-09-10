package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.PaymentException;
import kr.hhplus.be.server.exception.ReservationException;
import kr.hhplus.be.server.model.Payment;
import kr.hhplus.be.server.model.Reservation;
import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 결제 서비스 단위 테스트
 */
class PaymentServiceTest {

    private PaymentService paymentService;
    private ReservationService reservationService;
    private UserService userService;
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        // 저장소 초기화
        QueueTokenRepository queueTokenRepository = new QueueTokenRepository();
        ConcertRepository concertRepository = new ConcertRepository();
        SeatRepository seatRepository = new SeatRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        UserRepository userRepository = new UserRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        TransactionRepository transactionRepository = new TransactionRepository();

        // 서비스 초기화
        queueService = new QueueServiceImpl(queueTokenRepository);
        ConcertService concertService = new ConcertServiceImpl(concertRepository, seatRepository);
        concertService.initializeConcerts();
        
        reservationService = new ReservationServiceImpl(reservationRepository, seatRepository, concertRepository, queueService);
        userService = new UserServiceImpl(userRepository, transactionRepository, queueService);
        paymentService = new PaymentServiceImpl(paymentRepository, reservationRepository, seatRepository, transactionRepository, userService, queueService);
    }

    @Test
    void 결제_처리_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        // 잔액 충전
        userService.chargeBalance(tokenId, new BigDecimal("200000"));
        
        // 예약 생성
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // When
        Payment payment = paymentService.processPayment(tokenId, reservation.getReservationId());

        // Then
        assertNotNull(payment);
        assertEquals(userId, payment.getUserId());
        assertEquals(reservation.getReservationId(), payment.getReservationId());
        assertEquals(ticketPrice, payment.getAmount());
        assertTrue(payment.isCompleted());
        
        // 예약 상태 확인
        Reservation updatedReservation = reservationService.getReservation(tokenId, reservation.getReservationId());
        assertTrue(updatedReservation.isPaid());
    }

    @Test
    void 잔액_부족시_결제_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        // 잔액 부족하게 충전
        userService.chargeBalance(tokenId, new BigDecimal("50000"));
        
        // 예약 생성
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // When & Then
        assertThrows(PaymentException.InsufficientBalanceException.class, () -> {
            paymentService.processPayment(tokenId, reservation.getReservationId());
        });
    }

    @Test
    void 다른_사용자_예약_결제_실패() {
        // Given
        String userId1 = "user123";
        String userId2 = "user456";
        String tokenId1 = queueService.issueToken(userId1);
        String tokenId2 = queueService.issueToken(userId2);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        // user1이 예약 생성
        Reservation reservation = reservationService.holdSeat(tokenId1, concertDate, seatId, ticketPrice);
        
        // user2가 잔액 충전
        userService.chargeBalance(tokenId2, new BigDecimal("200000"));

        // When & Then
        assertThrows(ReservationException.class, () -> {
            paymentService.processPayment(tokenId2, reservation.getReservationId());
        });
    }

    @Test
    void 존재하지_않는_예약_결제_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String nonExistentReservationId = "non-existent-reservation-id";

        // When & Then
        assertThrows(ReservationException.class, () -> {
            paymentService.processPayment(tokenId, nonExistentReservationId);
        });
    }

    @Test
    void 결제_조회_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        userService.chargeBalance(tokenId, new BigDecimal("200000"));
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);
        Payment payment = paymentService.processPayment(tokenId, reservation.getReservationId());

        // When
        Payment foundPayment = paymentService.getPayment(tokenId, payment.getPaymentId());

        // Then
        assertNotNull(foundPayment);
        assertEquals(payment.getPaymentId(), foundPayment.getPaymentId());
        assertEquals(userId, foundPayment.getUserId());
    }

    @Test
    void 다른_사용자_결제_조회_실패() {
        // Given
        String userId1 = "user123";
        String userId2 = "user456";
        String tokenId1 = queueService.issueToken(userId1);
        String tokenId2 = queueService.issueToken(userId2);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        userService.chargeBalance(tokenId1, new BigDecimal("200000"));
        Reservation reservation = reservationService.holdSeat(tokenId1, concertDate, seatId, ticketPrice);
        Payment payment = paymentService.processPayment(tokenId1, reservation.getReservationId());

        // When & Then
        assertThrows(PaymentException.class, () -> {
            paymentService.getPayment(tokenId2, payment.getPaymentId());
        });
    }

    @Test
    void 사용자_결제_내역_조회_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        BigDecimal ticketPrice = new BigDecimal("100000");

        userService.chargeBalance(tokenId, new BigDecimal("300000"));
        
        Reservation reservation1 = reservationService.holdSeat(tokenId, concertDate, "1", ticketPrice);
        Reservation reservation2 = reservationService.holdSeat(tokenId, concertDate, "2", ticketPrice);
        
        paymentService.processPayment(tokenId, reservation1.getReservationId());
        paymentService.processPayment(tokenId, reservation2.getReservationId());

        // When
        var payments = paymentService.getUserPayments(tokenId);

        // Then
        assertEquals(2, payments.size());
        for (Payment payment : payments) {
            assertTrue(payment.getUserId().equals(userId));
            assertTrue(payment.isCompleted());
        }
    }

    @Test
    void 잘못된_입력값으로_결제_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(tokenId, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(tokenId, "");
        });
    }

    @Test
    void 거래_내역_포함_결제_처리_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        // 잔액 충전
        userService.chargeBalance(tokenId, new BigDecimal("200000"));
        
        // 예약 생성
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // When
        Payment payment = paymentService.processPaymentWithTransaction(tokenId, reservation.getReservationId());

        // Then
        assertNotNull(payment);
        assertEquals(userId, payment.getUserId());
        assertEquals(reservation.getReservationId(), payment.getReservationId());
        assertEquals(ticketPrice, payment.getAmount());
        assertTrue(payment.isCompleted());
        
        // 거래 내역 확인
        List<Transaction> transactions = userService.getUserTransactions(tokenId, 10);
        assertEquals(2, transactions.size()); // 충전 + 결제
        
        // 결제 거래 내역 확인
        Transaction paymentTransaction = transactions.stream()
            .filter(t -> t.getType() == Transaction.TransactionType.PAYMENT)
            .findFirst()
            .orElse(null);
        assertNotNull(paymentTransaction);
        assertEquals(ticketPrice, paymentTransaction.getAmount());
    }

    @Test
    void 동시성_결제_처리_테스트() throws InterruptedException {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        BigDecimal ticketPrice = new BigDecimal("50000");

        // 충분한 잔액 충전
        userService.chargeBalance(tokenId, new BigDecimal("1000000"));
        
        // 여러 예약 생성
        Reservation reservation1 = reservationService.holdSeat(tokenId, concertDate, "1", ticketPrice);
        Reservation reservation2 = reservationService.holdSeat(tokenId, concertDate, "2", ticketPrice);
        Reservation reservation3 = reservationService.holdSeat(tokenId, concertDate, "3", ticketPrice);

        // When - 동시에 결제 처리
        Thread[] threads = new Thread[3];
        Payment[] payments = new Payment[3];
        
        threads[0] = new Thread(() -> {
            payments[0] = paymentService.processPaymentWithTransaction(tokenId, reservation1.getReservationId());
        });
        threads[1] = new Thread(() -> {
            payments[1] = paymentService.processPaymentWithTransaction(tokenId, reservation2.getReservationId());
        });
        threads[2] = new Thread(() -> {
            payments[2] = paymentService.processPaymentWithTransaction(tokenId, reservation3.getReservationId());
        });

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then - 모든 결제가 성공해야 함
        for (Payment payment : payments) {
            assertNotNull(payment);
            assertTrue(payment.isCompleted());
        }
        
        // 최종 잔액 확인
        BigDecimal finalBalance = userService.getBalance(tokenId);
        BigDecimal expectedBalance = new BigDecimal("1000000").subtract(ticketPrice.multiply(new BigDecimal("3")));
        assertEquals(expectedBalance, finalBalance);
    }

    @Test
    void 토큰_만료_후_결제_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        userService.chargeBalance(tokenId, new BigDecimal("200000"));
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);
        
        // 토큰 만료
        queueService.expireToken(tokenId);

        // When & Then
        assertThrows(PaymentException.class, () -> {
            paymentService.processPaymentWithTransaction(tokenId, reservation.getReservationId());
        });
    }
}
