package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;

/**
 * 공연 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "performance_info")
public class PerformanceEntity {
    
    @Id
    @Column(name = "performance_id")
    private String performanceId;
    
    @Column(name = "concert_id", nullable = false)
    private String concertId;
    
    @Column(name = "venue_id", nullable = false)
    private String venueId;
    
    @Column(name = "performance_date", nullable = false)
    private LocalDate performanceDate;
    
    @Column(name = "performance_time", nullable = false)
    private LocalTime performanceTime;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Domain Model로 변환
     * 
     * 주의: ticketPrice는 performance_seat_pricing 테이블에서 관리되므로
     * Performance Domain Model에서 별도로 조회해야 합니다.
     */
    public kr.hhplus.be.server.model.Performance toDomain() {
        return new kr.hhplus.be.server.model.Performance(
            this.performanceId,
            this.concertId,
            this.venueId,
            this.performanceDate,
            this.performanceTime,
            java.math.BigDecimal.ZERO, // ticketPrice는 별도 조회 필요
            this.isActive
        );
    }
    
    /**
     * Domain Model로부터 생성
     */
    public static PerformanceEntity fromDomain(kr.hhplus.be.server.model.Performance performance) {
        PerformanceEntity entity = new PerformanceEntity();
        entity.performanceId = performance.getPerformanceId();
        entity.concertId = performance.getConcertId();
        entity.venueId = performance.getVenueId();
        entity.performanceDate = performance.getPerformanceDate();
        entity.performanceTime = performance.getPerformanceTime();
        entity.isActive = performance.isActive();
        entity.createdAt = performance.getCreatedAt();
        entity.updatedAt = performance.getLastUpdatedAt();
        return entity;
    }

    // Getters and Setters
    public String getPerformanceId() {
        return performanceId;
    }
    
    public void setPerformanceId(String performanceId) {
        this.performanceId = performanceId;
    }

    public String getConcertId() {
        return concertId;
    }
    
    public void setConcertId(String concertId) {
        this.concertId = concertId;
    }

    public String getVenueId() {
        return venueId;
    }
    
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public LocalDate getPerformanceDate() {
        return performanceDate;
    }
    
    public void setPerformanceDate(LocalDate performanceDate) {
        this.performanceDate = performanceDate;
    }

    public LocalTime getPerformanceTime() {
        return performanceTime;
    }
    
    public void setPerformanceTime(LocalTime performanceTime) {
        this.performanceTime = performanceTime;
    }

    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
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
