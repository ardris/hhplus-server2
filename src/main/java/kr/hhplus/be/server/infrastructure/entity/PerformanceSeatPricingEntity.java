package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 공연 좌석 가격 JPA 엔티티
 * 
 * DB 스키마: performance_seat_pricing 테이블
 * 공연별 좌석 등급별 가격 정보를 관리합니다.
 */
@Entity
@Table(name = "performance_seat_pricing")
public class PerformanceSeatPricingEntity {
    
    @Id
    @Column(name = "pricing_id", length = 50)
    private String pricingId;
    
    @Column(name = "performance_id", nullable = false, length = 50)
    private String performanceId;
    
    @Column(name = "seat_grade", nullable = false, length = 20)
    private String seatGrade; // S, A, B, C
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getPricingId() {
        return pricingId;
    }
    
    public void setPricingId(String pricingId) {
        this.pricingId = pricingId;
    }

    public String getPerformanceId() {
        return performanceId;
    }
    
    public void setPerformanceId(String performanceId) {
        this.performanceId = performanceId;
    }

    public String getSeatGrade() {
        return seatGrade;
    }
    
    public void setSeatGrade(String seatGrade) {
        this.seatGrade = seatGrade;
    }

    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
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
