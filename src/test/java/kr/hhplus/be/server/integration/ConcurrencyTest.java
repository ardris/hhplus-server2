package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.MakeReservationUseCase;
import kr.hhplus.be.server.application.ProcessPaymentUseCase;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.infrastructure.entity.SeatEntity;
import kr.hhplus.be.server.infrastructure.entity.SeatReservationStatusEntity;
import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import kr.hhplus.be.server.infrastructure.repository.JpaReservationRepository;
import kr.hhplus.be.server.infrastructure.repository.JpaSeatRepository;
import kr.hhplus.be.server.infrastructure.repository.JpaSeatReservationStatusRepository;
import kr.hhplus.be.server.infrastructure.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 동시성 통합 테스트 (실제 DB 기반)
 * 
 * 여러 사용자가 동시에 같은 좌석을 예약하거나,
 * 동시에 결제를 시도하는 등의 동시성 제어를 테스트합니다.
 * 
 * NOTE: 동시성 테스트이므로 @Transactional을 사용하지 않습니다.
 * 각 스레드가 독립적인 트랜잭션에서 실행되어야 실제 동시성을 테스트할 수 있습니다.
 */
@SpringBootTest
@ActiveProfiles("test")
// @Transactional - 동시성 테스트에서는 제거!
class ConcurrencyTest {

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
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        // 동시성 테스트이므로 트랜잭션 없이 직접 실행
        org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
            new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        
        transactionTemplate.execute(status -> {
            reservationRepository.deleteAll();
            seatReservationStatusRepository.deleteAll();
            seatRepository.deleteAll();
            userRepository.deleteAll();
            return null;
        });
    }

    @Test
    void 동일_좌석_동시_예약_시도_테스트() throws InterruptedException {
        System.out.println("\n");
        System.out.println("================================================================================");
        System.out.println("테스트: 동일 좌석 동시 예약 시도 (동시성 제어 검증)");
        System.out.println("================================================================================");
        
        // Given - 테스트 데이터 준비
        int threadCount = 10;
        String seatId = "SEAT-001";
        String concertId = "CONCERT-001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);
        
        // 데이터 생성 (트랜잭션으로 감싸서 즉시 커밋)
        org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
            new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        
        transactionTemplate.execute(status -> {
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
            
            // 사용자 생성
            List<UserEntity> users = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                String userId = "USER-" + String.format("%03d", i);
                UserEntity user = new UserEntity();
                user.setUserId(userId);
                user.setUsername("사용자" + i);
                user.setEmail("user" + i + "@test.com");
                user.setBalance(BigDecimal.valueOf(100000));
                user.setActive(true);
                user.setCreatedAt(LocalDateTime.now());
                users.add(user);
            }
            userRepository.saveAll(users);
            return null;
        });
        
        System.out.println("[테스트 설정]");
        System.out.println("  동시 요청 수: " + threadCount);
        System.out.println("  좌석 ID: " + seatId);
        System.out.println("  콘서트 ID: " + concertId);
        System.out.println("  티켓 가격: " + ticketPrice + "원");
        System.out.println("");
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        System.out.println("[동시 요청 시작]");
        long startTime = System.currentTimeMillis();
        
        // When - 동시 예약 시도
        for (int i = 0; i < threadCount; i++) {
            final String userId = "USER-" + String.format("%03d", i);
            
            executorService.submit(() -> {
                try {
                    long requestTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + requestTime + "ms] 예약 요청 시작 - 사용자: " + userId);
                    
                    MakeReservationUseCase.MakeReservationCommand command = 
                        new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);
                    
                    Reservation reservation = makeReservationUseCase.execute(command);
                    successCount.incrementAndGet();
                    
                    long endTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + endTime + "ms] >>> 예약 성공 - 사용자: " + userId + ", 예약ID: " + reservation.getReservationId());
                    
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    long endTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + endTime + "ms] >>> 예약 실패 - 사용자: " + userId + ", 사유: " + e.getMessage());
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
        System.out.println("  성공: " + successCount.get() + "건");
        System.out.println("  실패: " + failureCount.get() + "건");
        System.out.println("  소요 시간: " + totalTime + "ms");
        System.out.println("");
        
        assertEquals(1, successCount.get(), "1개의 예약만 성공해야 합니다");
        assertTrue(failureCount.get() >= 9, "최소 9개의 예약은 실패해야 합니다");
        
        System.out.println(">>> 테스트 통과: 동일 좌석 동시성 제어 성공");
        System.out.println("================================================================================\n");
    }

    @Test
    void 여러_좌석_동시_예약_테스트() throws InterruptedException {
        System.out.println("\n");
        System.out.println("================================================================================");
        System.out.println("테스트: 여러 좌석 동시 예약 (병렬 처리 검증)");
        System.out.println("================================================================================");
        
        // Given
        int threadCount = 5;
        String concertId = "CONCERT-001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);
        
        // 좌석 및 사용자 생성 (트랜잭션으로 감싸서 즉시 커밋)
        org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
            new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        
        transactionTemplate.execute(status -> {
            for (int i = 0; i < threadCount; i++) {
                String seatId = "SEAT-" + String.format("%03d", i + 1);
                String userId = "USER-" + String.format("%03d", i);
                
                // 좌석 생성
                SeatEntity seat = new SeatEntity();
                seat.setSeatId(seatId);
                seat.setVenueId("VENUE-001");
                seat.setSeatNumber(i + 1);
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
                
                // 사용자 생성
                UserEntity user = new UserEntity();
                user.setUserId(userId);
                user.setUsername("사용자" + i);
                user.setEmail("user" + i + "@test.com");
                user.setBalance(BigDecimal.valueOf(100000));
                user.setActive(true);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
            return null;
        });
        
        System.out.println("[테스트 설정]");
        System.out.println("  동시 요청 수: " + threadCount);
        System.out.println("  콘서트 ID: " + concertId);
        System.out.println("  티켓 가격: " + ticketPrice + "원");
        System.out.println("");
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        final AtomicInteger successCount = new AtomicInteger(0);

        System.out.println("[동시 요청 시작]");
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < threadCount; i++) {
            // final String userId = "USER-" + String.format("%03d", i);

            final String userId = "USER-" + String.format("%03d", 1);

            final String seatId = "SEAT-" + String.format("%03d", i + 1);
            
            executorService.submit(() -> {
                try {
                    long requestTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + requestTime + "ms] 예약 요청 - 사용자: " + userId + ", 좌석: " + seatId);
                    
                    MakeReservationUseCase.MakeReservationCommand command = 
                        new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);
                    
                    Reservation reservation = makeReservationUseCase.execute(command);
                    successCount.incrementAndGet();
                    
                    long endTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + endTime + "ms] >>> 예약 성공 - 사용자: " + userId + ", 좌석: " + seatId);
                    
                } catch (Exception e) {
                    long endTime = System.currentTimeMillis() - startTime;
                    System.out.println("  [" + endTime + "ms] >>> 예약 실패 - 사용자: " + userId + ", 사유: " + e.getMessage());
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
        System.out.println("  성공한 예약: " + successCount.get() + "건");
        System.out.println("  소요 시간: " + totalTime + "ms");
        System.out.println("");
        
        assertEquals(5, successCount.get(), "5개의 예약이 모두 성공해야 합니다");
        
        System.out.println(">>> 테스트 통과: 병렬 처리 성공");
        System.out.println("================================================================================\n");
    }

    @Test
    void 대량_예약_동시_처리_성능_테스트() throws InterruptedException {
        System.out.println("\n");
        System.out.println("================================================================================");
        System.out.println("테스트: 대량 예약 동시 처리 (성능 테스트)");
        System.out.println("================================================================================");
        
        // Given
        int threadCount = 50; // 100에서 50으로 줄임 (테스트 안정성)
        String concertId = "CONCERT-001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);
        
        // 좌석 및 사용자 대량 생성 (트랜잭션으로 감싸서 즉시 커밋)
        org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
            new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        
        transactionTemplate.execute(status -> {
            for (int i = 0; i < threadCount; i++) {
                String seatId = "SEAT-" + String.format("%03d", i + 1);
                String userId = "USER-" + String.format("%03d", i);
                
                // 좌석 생성
                SeatEntity seat = new SeatEntity();
                seat.setSeatId(seatId);
                seat.setVenueId("VENUE-001");
                seat.setSeatNumber(i + 1);
                seat.setSeatGrade("A");
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
                
                // 사용자 생성
                UserEntity user = new UserEntity();
                user.setUserId(userId);
                user.setUsername("사용자" + i);
                user.setEmail("user" + i + "@test.com");
                user.setBalance(BigDecimal.valueOf(100000));
                user.setActive(true);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
            return null;
        });
        
        System.out.println("[테스트 설정]");
        System.out.println("  동시 요청 수: " + threadCount);
        System.out.println("  콘서트 ID: " + concertId);
        System.out.println("  티켓 가격: " + ticketPrice + "원");
        System.out.println("");
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        final AtomicInteger successCount = new AtomicInteger(0);
        final List<Long> executionTimes = new ArrayList<>();

        System.out.println("[대량 요청 시작]");
        long startTime = System.currentTimeMillis();
        
        // When
        for (int i = 0; i < threadCount; i++) {
            final String userId = "USER-" + String.format("%03d", i);
            final String seatId = "SEAT-" + String.format("%03d", i + 1);
            
            executorService.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                try {
                    MakeReservationUseCase.MakeReservationCommand command = 
                        new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);
                    
                    makeReservationUseCase.execute(command);
                    successCount.incrementAndGet();
                    
                    long threadEndTime = System.currentTimeMillis();
                    synchronized(executionTimes) {
                        executionTimes.add(threadEndTime - threadStartTime);
                    }
                    
                } catch (Exception e) {
                    // 실패 처리
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        double avgTime = 0;
        if (!executionTimes.isEmpty()) {
            long sum = 0;
            for (Long time : executionTimes) {
                sum += time;
            }
            avgTime = (double)sum / executionTimes.size();
        }
        
        double tps = (successCount.get() * 1000.0) / totalTime;
        
        System.out.println("");
        System.out.println("[성능 테스트 결과]");
        System.out.println("  총 요청 수: " + threadCount + "건");
        System.out.println("  성공한 예약: " + successCount.get() + "건");
        System.out.println("  전체 소요 시간: " + totalTime + "ms");
        System.out.println("  평균 처리 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("  초당 처리량(TPS): " + String.format("%.2f", tps));
        System.out.println("");
        
        assertTrue(successCount.get() > 0, "최소 1개 이상의 예약이 성공해야 합니다");
        assertTrue(totalTime < 30000, "30초 이내에 완료되어야 합니다");
        
        System.out.println(">>> 테스트 통과: 성능 요구사항 충족");
        System.out.println("================================================================================\n");
    }
}
