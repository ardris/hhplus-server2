package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.MakeReservationUseCase;
import kr.hhplus.be.server.application.ProcessPaymentUseCase;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.infrastructure.entity.SeatEntity;
import kr.hhplus.be.server.infrastructure.entity.SeatReservationStatusEntity;
import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import kr.hhplus.be.server.infrastructure.repository.*;
import kr.hhplus.be.server.service.QueueService;
import kr.hhplus.be.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 전체 시나리오 통합 테스트 (실제 DB 기반)
 * 
 * 토큰 발급 → 잔액 충전 → 예약 생성 → 결제 완료까지의 전체 플로우를 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FullScenarioIntegrationTest {

    @Autowired
    private QueueService queueService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MakeReservationUseCase makeReservationUseCase;
    
    @Autowired
    private ProcessPaymentUseCase processPaymentUseCase;
    
    @Autowired
    private JpaUserRepository userRepository;
    
    @Autowired
    private JpaSeatRepository seatRepository;
    
    @Autowired
    private JpaSeatReservationStatusRepository seatReservationStatusRepository;
    
    @Autowired
    private JpaReservationRepository reservationRepository;
    
    @Autowired
    private JpaPaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        seatReservationStatusRepository.deleteAll();
        seatRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 토큰발급_잔액충전_예약_결제_전체_시나리오_테스트() {
        // ========== 1단계: 토큰 발급 ==========
        String userId = "user123";
        
        // 사용자 생성
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUsername("홍길동");
        user.setEmail("hong@test.com");
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // When - 토큰 발급
        String tokenId = queueService.issueToken(userId);
        
        // Then - 토큰 발급 검증
        assertNotNull(tokenId);
        System.out.println("[OK] 1단계: 토큰 발급 완료 - " + tokenId);

        // ========== 2단계: 잔액 충전 ==========
        BigDecimal chargeAmount = BigDecimal.valueOf(100000);
        
        // When - 잔액 충전
        BigDecimal newBalance = userService.chargeBalance(userId, chargeAmount);
        
        // Then - 잔액 충전 검증
        assertNotNull(newBalance);
        assertEquals(chargeAmount, newBalance);
        System.out.println("[OK] 2단계: 잔액 충전 완료 - " + newBalance + "원");

        // ========== 3단계: 좌석 예약 ==========
        String seatId = "seat001";
        String concertId = "concert001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);
        
        // 좌석 생성
        SeatEntity seat = new SeatEntity();
        seat.setSeatId(seatId);
        seat.setVenueId("VENUE-001");
        seat.setSeatNumber(1);
        seat.setSeatGrade("VIP");
        seat.setActive(true);
        seat.setCreatedAt(LocalDateTime.now());
        seatRepository.save(seat);
        
        // 좌석 예약 상태 생성
        SeatReservationStatusEntity seatStatus = new SeatReservationStatusEntity();
        seatStatus.setStatusId(UUID.randomUUID().toString());
        seatStatus.setSeatId(seatId);
        seatStatus.setPerformanceId("default");
        seatStatus.setSeatStatus("AVAILABLE");
        seatStatus.setCreatedAt(LocalDateTime.now());
        seatReservationStatusRepository.save(seatStatus);
        
        MakeReservationUseCase.MakeReservationCommand reservationCommand = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);
        
        // When - 예약 생성
        Reservation reservation = makeReservationUseCase.execute(reservationCommand);
        
        // Then - 예약 생성 검증
        assertNotNull(reservation);
        assertNotNull(reservation.getReservationId());
        assertEquals(userId, reservation.getUserId());
        assertEquals(seatId, reservation.getSeatId());
        assertEquals(ticketPrice, reservation.getTicketPrice());
        System.out.println("[OK] 3단계: 좌석 예약 완료 - 예약ID: " + reservation.getReservationId());

        // ========== 4단계: 결제 처리 ==========
        String reservationId = reservation.getReservationId();
        
        ProcessPaymentUseCase.ProcessPaymentCommand paymentCommand = 
            new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);
        
        // When - 결제 처리
        ProcessPaymentUseCase.PaymentResult paymentResult = processPaymentUseCase.execute(paymentCommand);
        
        // Then - 결제 완료 검증
        assertNotNull(paymentResult);
        assertTrue(paymentResult.isSuccess());
        assertNotNull(paymentResult.getPaymentId());
        System.out.println("[OK] 4단계: 결제 완료 - 결제ID: " + paymentResult.getPaymentId());

        // ========== 전체 플로우 검증 ==========
        // 최종 잔액 확인
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(BigDecimal.valueOf(50000).setScale(2), updatedUser.getBalance().setScale(2));
        
        System.out.println("[SUCCESS] 전체 시나리오 성공: 토큰발급 -> 잔액충전 -> 예약 -> 결제");
        System.out.println("  최종 잔액: " + updatedUser.getBalance() + "원");
    }

    @Test
    void 다중_좌석_예약_시나리오_테스트() {
        // ========== 1단계: 토큰 발급 ==========
        String userId = "user456";
        
        // 사용자 생성
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUsername("김철수");
        user.setEmail("kim@test.com");
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        String tokenId = queueService.issueToken(userId);
        assertNotNull(tokenId);
        System.out.println("[OK] 1단계: 토큰 발급 완료");

        // ========== 2단계: 잔액 충전 (3개 좌석 구매 가능한 금액) ==========
        BigDecimal chargeAmount = BigDecimal.valueOf(200000);
        BigDecimal newBalance = userService.chargeBalance(userId, chargeAmount);
        assertEquals(chargeAmount, newBalance);
        System.out.println("[OK] 2단계: 잔액 충전 완료 - " + newBalance + "원");

        // ========== 3단계: 3개 좌석 예약 ==========
        String concertId = "concert001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);
        String[] seatIds = {"seat001", "seat002", "seat003"};
        
        Reservation[] reservations = new Reservation[3];
        for (int i = 0; i < seatIds.length; i++) {
            // 좌석 생성
            SeatEntity seat = new SeatEntity();
            seat.setSeatId(seatIds[i]);
            seat.setVenueId("VENUE-001");
            seat.setSeatNumber(i + 1);
            seat.setSeatGrade("VIP");
            seat.setActive(true);
            seat.setCreatedAt(LocalDateTime.now());
            seatRepository.save(seat);
            
            // 좌석 예약 상태 생성
            SeatReservationStatusEntity seatStatus = new SeatReservationStatusEntity();
            seatStatus.setStatusId(UUID.randomUUID().toString());
            seatStatus.setSeatId(seatIds[i]);
            seatStatus.setPerformanceId("default");
            seatStatus.setSeatStatus("AVAILABLE");
            seatStatus.setCreatedAt(LocalDateTime.now());
            seatReservationStatusRepository.save(seatStatus);
            
            MakeReservationUseCase.MakeReservationCommand command = 
                new MakeReservationUseCase.MakeReservationCommand(userId, seatIds[i], concertId, ticketPrice);
            
            reservations[i] = makeReservationUseCase.execute(command);
            assertNotNull(reservations[i]);
            System.out.println("[OK] 3단계-" + (i+1) + ": 좌석 " + seatIds[i] + " 예약 완료");
        }

        // ========== 4단계: 각 예약에 대해 결제 ==========
        for (int i = 0; i < reservations.length; i++) {
            Reservation reservation = reservations[i];
            
            ProcessPaymentUseCase.ProcessPaymentCommand paymentCommand = 
                new ProcessPaymentUseCase.ProcessPaymentCommand(reservation.getReservationId());
            
            ProcessPaymentUseCase.PaymentResult result = processPaymentUseCase.execute(paymentCommand);
            
            assertTrue(result.isSuccess());
            System.out.println("[OK] 4단계-" + (i+1) + ": 좌석 " + seatIds[i] + " 결제 완료");
        }

        // ========== 전체 검증 ==========
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(BigDecimal.valueOf(50000).setScale(2), updatedUser.getBalance().setScale(2));
        
        System.out.println("[SUCCESS] 다중 좌석 예약 시나리오 성공: 3개 좌석 예약 및 결제 완료");
        System.out.println("  최종 잔액: " + updatedUser.getBalance() + "원");
    }

    @Test
    void 잔액_부족_시_결제_실패_시나리오() {
        // ========== 1단계: 토큰 발급 ==========
        String userId = "user999";
        
        // 사용자 생성
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUsername("이영희");
        user.setEmail("lee@test.com");
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        String tokenId = queueService.issueToken(userId);
        assertNotNull(tokenId);
        System.out.println("[OK] 1단계: 토큰 발급 완료");

        // ========== 2단계: 잔액 충전 (부족한 금액) ==========
        BigDecimal insufficientAmount = BigDecimal.valueOf(30000);
        BigDecimal balance = userService.chargeBalance(userId, insufficientAmount);
        System.out.println("[WARN] 2단계: 부족한 잔액 충전 - " + balance + "원 (필요: 50000원)");

        // ========== 3단계: 예약 생성 (성공) ==========
        String seatId = "seat001";
        String concertId = "concert001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);
        
        // 좌석 생성
        SeatEntity seat = new SeatEntity();
        seat.setSeatId(seatId);
        seat.setVenueId("VENUE-001");
        seat.setSeatNumber(1);
        seat.setSeatGrade("VIP");
        seat.setActive(true);
        seat.setCreatedAt(LocalDateTime.now());
        seatRepository.save(seat);
        
        // 좌석 예약 상태 생성
        SeatReservationStatusEntity seatStatus = new SeatReservationStatusEntity();
        seatStatus.setStatusId(UUID.randomUUID().toString());
        seatStatus.setSeatId(seatId);
        seatStatus.setPerformanceId("default");
        seatStatus.setSeatStatus("AVAILABLE");
        seatStatus.setCreatedAt(LocalDateTime.now());
        seatReservationStatusRepository.save(seatStatus);
        
        MakeReservationUseCase.MakeReservationCommand reservationCommand = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);
        
        Reservation reservation = makeReservationUseCase.execute(reservationCommand);
        assertNotNull(reservation);
        System.out.println("[OK] 3단계: 예약 생성 완료");

        // ========== 4단계: 결제 시도 (잔액 부족으로 실패) ==========
        String reservationId = reservation.getReservationId();
        
        ProcessPaymentUseCase.ProcessPaymentCommand paymentCommand = 
            new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);

        // When & Then - 결제 실패
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> processPaymentUseCase.execute(paymentCommand)
        );
        
        assertTrue(exception.getMessage().contains("잔액"));
        System.out.println("[OK] 4단계: 잔액 부족으로 결제 실패 확인: " + exception.getMessage());

        // 잔액이 그대로 유지되는지 확인
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(insufficientAmount.setScale(2), updatedUser.getBalance().setScale(2));
    }
}
