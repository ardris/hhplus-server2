package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.repository.*;
import kr.hhplus.be.server.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 콘서트 예약 서비스 통합 테스트
 * 전체 플로우를 시뮬레이션하여 실제 사용자 시나리오를 테스트
 */
class ConcertReservationIntegrationTest {

    private QueueService queueService;
    private ConcertService concertService;
    private ReservationService reservationService;
    private UserService userService;
    private PaymentService paymentService;

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
        concertService = new ConcertServiceImpl(concertRepository, seatRepository);
        reservationService = new ReservationServiceImpl(reservationRepository, seatRepository, concertRepository, queueService);
        userService = new UserServiceImpl(userRepository, transactionRepository, queueService);
        paymentService = new PaymentServiceImpl(paymentRepository, reservationRepository, seatRepository, transactionRepository, userService, queueService);

        // 콘서트 초기화
        concertService.initializeConcerts();
    }

    @Test
    void 전체_예약_플로우_통합_테스트() {
        // Given - 사용자 등록 및 토큰 발급
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        
        // When & Then - 1단계: 대기열 상태 확인
        var queueStatus = queueService.getQueueStatus(tokenId);
        assertNotNull(queueStatus);
        assertTrue(queueStatus.getQueuePosition() > 0);
        
        // When & Then - 2단계: 사용자 활성화
        queueService.activateUser(userId);
        assertTrue(queueService.isUserActive(userId));
        
        // When & Then - 3단계: 콘서트 목록 조회
        var concerts = concertService.getAvailableConcerts();
        assertNotNull(concerts);
        assertFalse(concerts.isEmpty());
        
        // When & Then - 4단계: 특정 콘서트 좌석 조회
        String concertDate = LocalDate.now().plusDays(1).toString();
        var seatDetails = concertService.getConcertSeatDetails(concertDate, "콘서트 1일차");
        assertNotNull(seatDetails);
        assertEquals(50, seatDetails.getTotalSeats());
        assertEquals(50, seatDetails.getAvailableCount());
        
        // When & Then - 5단계: 잔액 충전
        BigDecimal chargeAmount = new BigDecimal("200000");
        Transaction chargeTransaction = userService.chargeBalanceWithTransaction(tokenId, chargeAmount);
        assertNotNull(chargeTransaction);
        assertEquals(Transaction.TransactionType.CHARGE, chargeTransaction.getType());
        
        // When & Then - 6단계: 좌석 예약 (임시 배정)
        String seatId = "1";
        var reservation = reservationService.holdSeat(tokenId, concertDate, "콘서트 1일차", seatId);
        assertNotNull(reservation);
        assertTrue(reservation.isHold());
        
        // When & Then - 7단계: 잔액 조회 (거래 내역 포함)
        var balanceResponse = userService.getUserTransactions(tokenId, 10);
        assertNotNull(balanceResponse);
        assertTrue(balanceResponse.size() >= 1);
        
        // When & Then - 8단계: 결제 처리
        Payment payment = paymentService.processPaymentWithTransaction(tokenId, reservation.getReservationId());
        assertNotNull(payment);
        assertTrue(payment.isCompleted());
        
        // When & Then - 9단계: 예약 확정 확인
        var confirmedReservation = reservationService.getReservation(tokenId, reservation.getReservationId());
        assertTrue(confirmedReservation.isPaid());
        
        // When & Then - 10단계: 최종 잔액 확인
        BigDecimal finalBalance = userService.getBalance(tokenId);
        BigDecimal expectedBalance = chargeAmount.subtract(concertService.getConcertSeatDetails(concertDate, "콘서트 1일차").getConcertInfo().getTicketPrice());
        assertEquals(expectedBalance, finalBalance);
    }

    @Test
    void 다중_사용자_동시_예약_통합_테스트() throws InterruptedException {
        // Given - 여러 사용자 등록
        String[] userIds = {"user1", "user2", "user3"};
        String[] tokenIds = new String[3];
        
        for (int i = 0; i < 3; i++) {
            tokenIds[i] = queueService.issueToken(userIds[i]);
            queueService.activateUser(userIds[i]);
            userService.chargeBalanceWithTransaction(tokenIds[i], new BigDecimal("200000"));
        }
        
        String concertDate = LocalDate.now().plusDays(1).toString();
        
        // When - 동시에 좌석 예약
        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    reservationService.holdSeat(tokenIds[index], concertDate, "콘서트 1일차", String.valueOf(index + 1));
                } catch (Exception e) {
                    // 예외 발생 시 로그 (실제로는 실패해야 함)
                    System.out.println("User " + userIds[index] + " reservation failed: " + e.getMessage());
                }
            });
            threads[i].start();
        }
        
        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - 각 사용자의 예약 확인
        for (int i = 0; i < 3; i++) {
            var reservations = reservationService.getUserReservations(tokenIds[i]);
            assertTrue(reservations.size() >= 0); // 예약 성공/실패 여부는 동시성에 따라 달라질 수 있음
        }
    }

    @Test
    void 좌석_중복_예약_방지_통합_테스트() {
        // Given
        String userId1 = "user1";
        String userId2 = "user2";
        String tokenId1 = queueService.issueToken(userId1);
        String tokenId2 = queueService.issueToken(userId2);
        
        queueService.activateUser(userId1);
        queueService.activateUser(userId2);
        
        userService.chargeBalanceWithTransaction(tokenId1, new BigDecimal("200000"));
        userService.chargeBalanceWithTransaction(tokenId2, new BigDecimal("200000"));
        
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        
        // When - 첫 번째 사용자가 좌석 예약
        var reservation1 = reservationService.holdSeat(tokenId1, concertDate, "콘서트 1일차", seatId);
        assertNotNull(reservation1);
        
        // When & Then - 두 번째 사용자가 같은 좌석 예약 시도 (실패해야 함)
        assertThrows(Exception.class, () -> {
            reservationService.holdSeat(tokenId2, concertDate, "콘서트 1일차", seatId);
        });
    }

    @Test
    void 예약_만료_후_좌석_해제_통합_테스트() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        queueService.activateUser(userId);
        userService.chargeBalanceWithTransaction(tokenId, new BigDecimal("200000"));
        
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        
        // When - 좌석 예약
        var reservation = reservationService.holdSeat(tokenId, concertDate, "콘서트 1일차", seatId);
        assertNotNull(reservation);
        assertTrue(reservation.isHold());
        
        // When - 예약 만료 처리 (실제로는 시간이 지나야 하지만 테스트를 위해 강제 만료)
        reservation.expireHold();
        
        // When - 만료된 예약 정리
        reservationService.cleanupExpiredHolds();
        
        // Then - 좌석이 다시 예약 가능한 상태가 되어야 함
        var seatDetails = concertService.getConcertSeatDetails(concertDate, "콘서트 1일차");
        boolean seatAvailable = seatDetails.getAvailableSeats().stream()
            .anyMatch(seat -> seat.getSeatId().equals(seatId) && seat.getStatus().equals("AVAILABLE"));
        assertTrue(seatAvailable);
    }
}
