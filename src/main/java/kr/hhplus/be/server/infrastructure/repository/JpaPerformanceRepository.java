package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.PerformanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 공연 JPA Repository
 */
@Repository
public interface JpaPerformanceRepository extends JpaRepository<PerformanceEntity, String> {
    
    @Query("SELECT p FROM PerformanceEntity p WHERE p.concertId = :concertId")
    List<PerformanceEntity> findByConcertId(@Param("concertId") String concertId);
    
    @Query("SELECT p FROM PerformanceEntity p WHERE p.venueId = :venueId")
    List<PerformanceEntity> findByVenueId(@Param("venueId") String venueId);
    
    @Query("SELECT p FROM PerformanceEntity p WHERE p.performanceDate = :date")
    List<PerformanceEntity> findByPerformanceDate(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM PerformanceEntity p WHERE p.isActive = true")
    List<PerformanceEntity> findActivePerformances();
    
    /**
     * 특정 기간에 공연이 있는 공연장 ID 목록 조회
     */
    @Query("SELECT DISTINCT p.venueId FROM PerformanceEntity p WHERE p.performanceDate BETWEEN :startDate AND :endDate")
    List<String> findVenueIdsWithPerformancesInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
