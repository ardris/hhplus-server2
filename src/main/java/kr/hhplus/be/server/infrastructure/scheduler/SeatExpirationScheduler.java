package kr.hhplus.be.server.infrastructure.scheduler;

import kr.hhplus.be.server.infrastructure.repository.JpaSeatReservationStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 좌석 만료 스케줄러
 * 
 * 5분이 지난 임시 배정(HOLD) 좌석들을 자동으로 AVAILABLE 상태로 변경합니다.
 * Infrastructure Layer에서 데이터베이스 정합성을 보장하는 역할을 합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeatExpirationScheduler {
    
    private final JpaSeatReservationStatusRepository jpaSeatReservationStatusRepository;
    
    /**
     * 만료된 좌석들을 자동으로 해제합니다.
     * 
     * 매 1분마다 실행되어 만료된 HOLD 상태의 좌석들을 AVAILABLE로 변경합니다.
     * 이는 Redis 없이도 DB 기반으로 좌석 상태를 관리할 수 있게 해줍니다.
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void releaseExpiredSeats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int releasedCount = jpaSeatReservationStatusRepository.releaseExpiredHolds(now);
            
            if (releasedCount > 0) {
                log.info("만료된 좌석 {}개를 자동으로 해제했습니다.", releasedCount);
            }
        } catch (Exception e) {
            log.error("좌석 만료 처리 중 오류가 발생했습니다.", e);
        }
    }
}

