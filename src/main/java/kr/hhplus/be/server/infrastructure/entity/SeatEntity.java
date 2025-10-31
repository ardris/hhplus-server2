package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 좌석 JPA 엔티티
 * 
 * DB 스키마: seat_info 테이블
 * 공연장의 물리적 좌석 정보를 관리합니다.
 */
@Entity
@Table(name = "seat_info")
public class SeatEntity {
    
    @Id
    @Column(name = "seat_id", length = 20)
    private String seatId;
    
    @Column(name = "venue_id", nullable = false, length = 50)
    private String venueId;
    
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
    
    @Column(name = "seat_grade", nullable = false, length = 20)
    private String seatGrade; // S, A, B, C
    
    @Column(name = "seat_section", length = 50)
    private String seatSection; // 1층, 2층, VIP 등
    
    @Column(name = "seat_row", length = 10)
    private String seatRow;
    
    @Column(name = "seat_column", length = 10)
    private String seatColumn;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public String getSeatId() {
        return seatId;
    }
    
    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getVenueId() {
        return venueId;
    }
    
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatGrade() {
        return seatGrade;
    }
    
    public void setSeatGrade(String seatGrade) {
        this.seatGrade = seatGrade;
    }

    public String getSeatSection() {
        return seatSection;
    }
    
    public void setSeatSection(String seatSection) {
        this.seatSection = seatSection;
    }

    public String getSeatRow() {
        return seatRow;
    }
    
    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public String getSeatColumn() {
        return seatColumn;
    }
    
    public void setSeatColumn(String seatColumn) {
        this.seatColumn = seatColumn;
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