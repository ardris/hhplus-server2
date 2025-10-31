package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 좌석 예약 상태 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "seat_reservation_status")
public class SeatReservationStatusEntity {
    
    @Id
    @Column(name = "status_id")
    private String statusId;
    
    @Column(name = "seat_id", nullable = false)
    private String seatId;
    
    @Column(name = "performance_id", nullable = false)
    private String performanceId;
    
    @Column(name = "seat_status", nullable = false)
    private String seatStatus;
    
    @Column(name = "held_by_reservation_id")
    private String heldByReservationId;
    
    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getStatusId() {
        return statusId;
    }
    
    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getSeatId() {
        return seatId;
    }
    
    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getPerformanceId() {
        return performanceId;
    }
    
    public void setPerformanceId(String performanceId) {
        this.performanceId = performanceId;
    }

    public String getSeatStatus() {
        return seatStatus;
    }
    
    public void setSeatStatus(String seatStatus) {
        this.seatStatus = seatStatus;
    }

    public String getHeldByReservationId() {
        return heldByReservationId;
    }
    
    public void setHeldByReservationId(String heldByReservationId) {
        this.heldByReservationId = heldByReservationId;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }
    
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
