package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.MakeReservationUseCase;
import kr.hhplus.be.server.application.ProcessPaymentUseCase;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.infrastructure.entity.SeatEntity;
import kr.hhplus.be.server.infrastructure.entity.SeatReservationStatusEntity;
import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import kr.hhplus.be.server.infrastructure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 예약-결제 통합 테스트 (실제 DB 기반)
 * 
 * 예약 생성부터 결제 완료까지의 전체 플로우를 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationPaymentIntegrationTest {

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
    void VIP좌석_예약_및_결제_테스트() {
        System.out.println("\n================================================================================");
        System.out.println("테스트: VIP 좌석 예약 및 결제");
        System.out.println("================================================================================");
        
        // Given
        String userId = "user123";
        String seatId = "A-001";
        String seatGrade = "VIP";
        String concertId = "CONCERT-2024-001";
        String concertName = "BTS 월드투어 서울";
        BigDecimal vipPrice = BigDecimal.valueOf(150000);

        System.out.println("[테스트 데이터]");
        System.out.println("  사용자 ID: " + userId);
        System.out.println("  콘서트: " + concertName);
        System.out.println("  좌석 번호: " + seatId);
        System.out.println("  좌석 등급: " + seatGrade);
        System.out.println("  티켓 가격: " + vipPrice + "원");
        System.out.println("");

        // 사용자 생성
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUsername("홍길동");
        user.setEmail("hong@test.com");
        user.setBalance(BigDecimal.valueOf(200000));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 좌석 생성
        SeatEntity seat = new SeatEntity();
        seat.setSeatId(seatId);
        seat.setVenueId("VENUE-001");
        seat.setSeatNumber(1);
        seat.setSeatGrade(seatGrade);
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

        // When - 예약 생성
        System.out.println("[1단계: VIP 좌석 예약]");
        MakeReservationUseCase.MakeReservationCommand reservationCommand = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, vipPrice);
        
        Reservation reservation = makeReservationUseCase.execute(reservationCommand);
        
        assertNotNull(reservation);
        assertEquals(seatId, reservation.getSeatId());
        assertEquals(vipPrice, reservation.getTicketPrice());
        System.out.println("  >>> 예약 완료: " + reservation.getReservationId());
        System.out.println("");

        // When - 결제
        System.out.println("[2단계: VIP 좌석 결제]");
        String reservationId = reservation.getReservationId();

        ProcessPaymentUseCase.ProcessPaymentCommand paymentCommand = 
            new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);
        
        ProcessPaymentUseCase.PaymentResult paymentResult = processPaymentUseCase.execute(paymentCommand);
        
        assertTrue(paymentResult.isSuccess());
        System.out.println("  >>> 결제 완료: " + paymentResult.getPaymentId());
        System.out.println("");
        
        // 최종 잔액 확인
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(BigDecimal.valueOf(50000).setScale(2), updatedUser.getBalance().setScale(2));
        
        System.out.println(">>> 테스트 통과: VIP 좌석 예약 및 결제 성공");
        System.out.println("================================================================================\n");
    }

    @Test
    void 다양한_등급_좌석_동시_예약_테스트() {
        System.out.println("\n================================================================================");
        System.out.println("테스트: 다양한 등급 좌석 동시 예약");
        System.out.println("================================================================================");
        
        // Given
        String userId = "user456";
        String concertId = "CONCERT-2024-002";
        String concertName = "아이유 콘서트";
        
        // 다양한 좌석 등급
        String[][] seatInfo = {
            {"A-001", "VIP", "150000"},
            {"B-012", "R석", "100000"},
            {"C-045", "S석", "80000"},
            {"D-099", "A석", "50000"}
        };

        System.out.println("[테스트 데이터]");
        System.out.println("  사용자 ID: " + userId);
        System.out.println("  콘서트: " + concertName);
        System.out.println("  예약 좌석:");
        for (String[] seat : seatInfo) {
            System.out.println("    - " + seat[0] + " (" + seat[1] + "): " + seat[2] + "원");
        }
        System.out.println("");

        // 사용자 생성
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUsername("김철수");
        user.setEmail("kim@test.com");
        user.setBalance(BigDecimal.valueOf(500000));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 좌석들 생성
        int seatNum = 1;
        for (String[] info : seatInfo) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatId(info[0]);
            seat.setVenueId("VENUE-001");
            seat.setSeatNumber(seatNum++);
            seat.setSeatGrade(info[1]);
            seat.setActive(true);
            seat.setCreatedAt(LocalDateTime.now());
            seatRepository.save(seat);
            
            // 좌석 예약 상태 생성
            SeatReservationStatusEntity seatStatus = new SeatReservationStatusEntity();
            seatStatus.setStatusId(UUID.randomUUID().toString());
            seatStatus.setSeatId(info[0]);
            seatStatus.setPerformanceId("default");
            seatStatus.setSeatStatus("AVAILABLE");
            seatStatus.setCreatedAt(LocalDateTime.now());
            seatReservationStatusRepository.save(seatStatus);
        }

        // When - 각 등급별 예약 및 결제
        System.out.println("[예약 및 결제 진행]");
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (int i = 0; i < seatInfo.length; i++) {
            String seatId = seatInfo[i][0];
            String grade = seatInfo[i][1];
            BigDecimal price = new BigDecimal(seatInfo[i][2]);
            
            System.out.println("  [" + (i+1) + "번째 좌석]");
            
            // 예약
            MakeReservationUseCase.MakeReservationCommand reservationCommand = 
                new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, price);
            
            Reservation reservation = makeReservationUseCase.execute(reservationCommand);
            assertNotNull(reservation);
            System.out.println("    >>> 예약: " + seatId + " (" + grade + ") - " + price + "원");
            
            // 결제
            ProcessPaymentUseCase.ProcessPaymentCommand paymentCommand = 
                new ProcessPaymentUseCase.ProcessPaymentCommand(reservation.getReservationId());
            
            ProcessPaymentUseCase.PaymentResult result = processPaymentUseCase.execute(paymentCommand);
            assertTrue(result.isSuccess());
            System.out.println("    >>> 결제 완료: " + result.getPaymentId());
            
            totalAmount = totalAmount.add(price);
        }
        
        System.out.println("");
        System.out.println("  총 결제 금액: " + totalAmount + "원");
        System.out.println("");
        
        // 최종 잔액 확인
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        BigDecimal expectedBalance = BigDecimal.valueOf(500000).subtract(totalAmount);
        assertEquals(expectedBalance.setScale(2), updatedUser.getBalance().setScale(2));
        
        System.out.println(">>> 테스트 통과: 4개 등급 좌석 예약 및 결제 성공");
        System.out.println("================================================================================\n");
    }

    @Test
    void 동일_VIP좌석_동시_예약_경쟁_테스트() throws InterruptedException {
        System.out.println("\n================================================================================");
        System.out.println("테스트: 동일 VIP 좌석 동시 예약 경쟁 (동시성 제어)");
        System.out.println("================================================================================");
        
        // Given
        int threadCount = 5;
        String seatId = "VIP-A-001";
        String seatGrade = "VIP";
        String concertId = "CONCERT-2024-HOT";
        String concertName = "방탄소년단 앵콜 콘서트";
        BigDecimal vipPrice = BigDecimal.valueOf(200000);
        
        System.out.println("[테스트 데이터]");
        System.out.println("  콘서트: " + concertName);
        System.out.println("  좌석: " + seatId + " (" + seatGrade + ")");
        System.out.println("  가격: " + vipPrice + "원");
        System.out.println("  동시 예약 시도: " + threadCount + "명");
        System.out.println("  예상: 1명만 성공");
        System.out.println("");

        // 좌석 생성
        SeatEntity seat = new SeatEntity();
        seat.setSeatId(seatId);
        seat.setVenueId("VENUE-001");
        seat.setSeatNumber(1);
        seat.setSeatGrade(seatGrade);
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
        
        // 사용자들 생성
        for (int i = 0; i < threadCount; i++) {
            String userId = "USER-" + String.format("%03d", i);
            UserEntity user = new UserEntity();
            user.setUserId(userId);
            user.setUsername("사용자" + i);
            user.setEmail("user" + i + "@test.com");
            user.setBalance(BigDecimal.valueOf(300000));
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        System.out.println("[동시 예약 시도 시작]");
        long startTime = System.currentTimeMillis();
        
        // When - 동시 예약 시도
        for (int i = 0; i < threadCount; i++) {
            final String userId = "USER-" + String.format("%03d", i);
            
            executorService.submit(() -> {
                try {
                    long requestTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + requestTime + "ms] " + userId + " - VIP 좌석 " + seatId + " 예약 시도");
                    
                    MakeReservationUseCase.MakeReservationCommand command = 
                        new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, vipPrice);
                    
                    Reservation reservation = makeReservationUseCase.execute(command);
                    successCount.incrementAndGet();
                    
                    long endTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + endTime + "ms] >>> 예약 성공: " + userId + " - " + reservation.getReservationId());
                    
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    long endTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + endTime + "ms] >>> 예약 실패: " + userId + " - " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        System.out.println("");
        System.out.println("[테스트 결과]");
        System.out.println("  성공: " + successCount.get() + "명");
        System.out.println("  실패: " + failureCount.get() + "명");
        System.out.println("  소요 시간: " + totalTime + "ms");
        System.out.println("");
        
        assertEquals(1, successCount.get(), "1명만 VIP 좌석 예약에 성공해야 합니다");
        assertTrue(failureCount.get() >= 4, "최소 4명은 예약에 실패해야 합니다");
        
        System.out.println(">>> 테스트 통과: VIP 좌석 동시성 제어 성공");
        System.out.println("================================================================================\n");
    }

    @Test
    void 좌석_불가_시_예약_실패하고_결제_진행_안됨() {
        System.out.println("\n================================================================================");
        System.out.println("테스트: 좌석 불가 시 예약 실패 및 결제 미진행");
        System.out.println("================================================================================");
        
        // Given
        String userId = "user123";
        String seatId = "R-A-050";
        String seatGrade = "R석";
        String concertId = "concert001";
        BigDecimal ticketPrice = BigDecimal.valueOf(100000);

        System.out.println("[테스트 데이터]");
        System.out.println("  사용자 ID: " + userId);
        System.out.println("  좌석 ID: " + seatId + " (" + seatGrade + ") - 이미 예약됨");
        System.out.println("  콘서트 ID: " + concertId);
        System.out.println("  가격: " + ticketPrice + "원");
        System.out.println("");

        // 사용자 생성
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUsername("박영희");
        user.setEmail("park@test.com");
        user.setBalance(BigDecimal.valueOf(200000));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 좌석 생성 (이미 예약된 상태)
        SeatEntity seat = new SeatEntity();
        seat.setSeatId(seatId);
        seat.setVenueId("VENUE-001");
        seat.setSeatNumber(1);
        seat.setSeatGrade(seatGrade);
        seat.setActive(true);
        seat.setCreatedAt(LocalDateTime.now());
        seatRepository.save(seat);
        
        // 좌석 예약 상태 - SOLD (판매 완료)
        SeatReservationStatusEntity seatStatus = new SeatReservationStatusEntity();
        seatStatus.setStatusId(UUID.randomUUID().toString());
        seatStatus.setSeatId(seatId);
        seatStatus.setPerformanceId("default");
        seatStatus.setSeatStatus("SOLD");
        seatStatus.setCreatedAt(LocalDateTime.now());
        seatReservationStatusRepository.save(seatStatus);

        MakeReservationUseCase.MakeReservationCommand command = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);

        // When & Then
        System.out.println("[예약 시도]");
        assertThrows(IllegalStateException.class, () -> {
            makeReservationUseCase.execute(command);
        });
        System.out.println("  >>> 예약 실패: 좌석 " + seatId + "(" + seatGrade + ")는 이미 예약되었습니다");
        System.out.println("");

        // 결제가 발생하지 않았는지 확인
        long paymentCount = paymentRepository.count();
        assertEquals(0, paymentCount);
        
        System.out.println(">>> 테스트 통과: 좌석 불가 시 예약 및 결제 미진행 확인");
        System.out.println("================================================================================\n");
    }

    @Test
    void 잔액_부족_시_결제_실패() {
        System.out.println("\n================================================================================");
        System.out.println("테스트: 잔액 부족 시 결제 실패");
        System.out.println("================================================================================");
        
        // Given
        String userId = "user123";
        String seatId = "VIP-A-001";
        String seatGrade = "VIP";
        String concertId = "concert001";
        BigDecimal vipPrice = BigDecimal.valueOf(200000);
        BigDecimal userBalance = BigDecimal.valueOf(150000);

        System.out.println("[테스트 데이터]");
        System.out.println("  사용자 ID: " + userId);
        System.out.println("  좌석 ID: " + seatId + " (" + seatGrade + ")");
        System.out.println("  티켓 가격: " + vipPrice + "원");
        System.out.println("  사용자 잔액: " + userBalance + "원");
        System.out.println("  부족 금액: " + vipPrice.subtract(userBalance) + "원");
        System.out.println("");

        // 사용자 생성 (잔액 부족)
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setUsername("최민수");
        user.setEmail("choi@test.com");
        user.setBalance(userBalance);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 좌석 생성
        SeatEntity seat = new SeatEntity();
        seat.setSeatId(seatId);
        seat.setVenueId("VENUE-001");
        seat.setSeatNumber(1);
        seat.setSeatGrade(seatGrade);
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

        System.out.println("[1단계: 예약 생성]");
        MakeReservationUseCase.MakeReservationCommand reservationCommand = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, vipPrice);
        
        Reservation reservation = makeReservationUseCase.execute(reservationCommand);
        assertNotNull(reservation);
        System.out.println("  >>> 예약 성공: " + reservation.getReservationId() + " - " + seatId + " (" + seatGrade + ")");
        System.out.println("");

        // Given - 잔액 부족으로 결제 실패
        String reservationId = reservation.getReservationId();

        ProcessPaymentUseCase.ProcessPaymentCommand paymentCommand = 
            new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);

        // When & Then
        System.out.println("[2단계: 결제 시도]");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processPaymentUseCase.execute(paymentCommand);
        });
        
        System.out.println("  >>> 결제 실패: " + exception.getMessage());
        System.out.println("");
        
        // 잔액이 그대로 유지되는지 확인
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(userBalance.setScale(2), updatedUser.getBalance().setScale(2));
        
        System.out.println(">>> 테스트 통과: 잔액 부족 시 결제 실패 확인");
        System.out.println("================================================================================\n");
    }
}
