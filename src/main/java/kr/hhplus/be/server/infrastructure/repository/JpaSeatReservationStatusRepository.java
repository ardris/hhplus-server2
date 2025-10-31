package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.SeatReservationStatusEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 좌석 예약 상태 JPA Repository
 */
@Repository
public interface JpaSeatReservationStatusRepository extends JpaRepository<SeatReservationStatusEntity, String> {
    
    /**
     * 좌석 예약 상태 조회 (비관적 락)
     * 동시성 제어를 위해 SELECT FOR UPDATE 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatReservationStatusEntity s WHERE s.seatId = :seatId AND s.performanceId = :performanceId")
    SeatReservationStatusEntity findBySeatIdAndPerformanceId(@Param("seatId") String seatId, @Param("performanceId") String performanceId);
    
    @Query("SELECT s FROM SeatReservationStatusEntity s WHERE s.performanceId = :performanceId AND s.seatStatus = :status")
    List<SeatReservationStatusEntity> findByPerformanceIdAndStatus(@Param("performanceId") String performanceId, @Param("status") String status);
    
    @Query("SELECT s FROM SeatReservationStatusEntity s WHERE s.holdExpiresAt < :now")
    List<SeatReservationStatusEntity> findExpiredHolds(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE SeatReservationStatusEntity s SET s.seatStatus = 'AVAILABLE', s.heldByReservationId = null, s.holdExpiresAt = null WHERE s.holdExpiresAt < :now")
    int releaseExpiredHolds(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE SeatReservationStatusEntity s SET s.seatStatus = :status, s.heldByReservationId = :reservationId, s.holdExpiresAt = :expiresAt WHERE s.seatId = :seatId AND s.performanceId = :performanceId")
    int updateSeatStatus(@Param("seatId") String seatId, @Param("performanceId") String performanceId, 
                        @Param("status") String status, @Param("reservationId") String reservationId, 
                        @Param("expiresAt") LocalDateTime expiresAt);
}
