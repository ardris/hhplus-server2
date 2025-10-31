package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.PerformanceSeatPricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 공연 좌석 가격 JPA Repository
 */
@Repository
public interface JpaPerformanceSeatPricingRepository extends JpaRepository<PerformanceSeatPricingEntity, String> {
    
    /**
     * 공연 ID로 모든 좌석 가격 조회
     */
    List<PerformanceSeatPricingEntity> findByPerformanceId(String performanceId);
    
    /**
     * 공연 ID와 좌석 등급으로 가격 조회
     */
    Optional<PerformanceSeatPricingEntity> findByPerformanceIdAndSeatGrade(String performanceId, String seatGrade);
}

