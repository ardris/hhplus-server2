package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 예약 JPA Repository
 * 
 * Infrastructure Layer에서 데이터베이스와 직접 소통하는 Repository입니다.
 * JPA를 사용하여 CRUD 작업을 수행합니다.
 */
@Repository
public interface JpaReservationRepository extends JpaRepository<ReservationEntity, String> {
    
    /**
     * 사용자 ID로 예약 목록 조회
     */
    List<ReservationEntity> findByUserId(String userId);
    
    /**
     * 공연 ID로 예약 목록 조회
     */
    List<ReservationEntity> findByPerformanceId(String performanceId);
    
    /**
     * 공연 ID와 상태로 예약 목록 조회
     */
    List<ReservationEntity> findByPerformanceIdAndStatus(String performanceId, String status);
    
    
    /**
     * 만료된 임시 예약 조회 (좌석 해제용)
     */
    @Query("SELECT r FROM ReservationEntity r WHERE r.status = 'HOLD' AND r.holdExpiresAt < :now")
    List<ReservationEntity> findExpiredHoldReservations(@Param("now") LocalDateTime now);
    
    /**
     * 예약 상태별 조회
     */
    @Query("SELECT r FROM ReservationEntity r WHERE r.status = :status")
    List<ReservationEntity> findByStatus(@Param("status") String status);
    
    /**
     * 좌석 ID로 활성 예약 조회
     */
    @Query("SELECT r FROM ReservationEntity r WHERE r.seatId = :seatId AND r.status IN ('HOLD', 'PAID')")
    Optional<ReservationEntity> findActiveReservationBySeatId(@Param("seatId") String seatId);
}
