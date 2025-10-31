package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 콘서트 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "concert_master_info")
public class ConcertEntity {
    
    @Id
    @Column(name = "concert_id")
    private String concertId;
    
    @Column(name = "concert_name", nullable = false)
    private String concertName;
    
    @Column(name = "concert_period_start", nullable = false)
    private LocalDate concertPeriodStart;
    
    @Column(name = "concert_period_end", nullable = false)
    private LocalDate concertPeriodEnd;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Domain Model로 변환
     */
    public kr.hhplus.be.server.model.Concert toDomain() {
        return new kr.hhplus.be.server.model.Concert(
            this.concertId,
            this.concertName,
            this.concertPeriodStart,
            this.concertPeriodEnd,
            this.isActive,
            this.createdAt,
            this.updatedAt
        );
    }
    
    /**
     * Domain Model로부터 생성
     */
    public static ConcertEntity fromDomain(kr.hhplus.be.server.model.Concert concert) {
        ConcertEntity entity = new ConcertEntity();
        entity.concertId = concert.getConcertId();
        entity.concertName = concert.getConcertName();
        entity.concertPeriodStart = concert.getConcertPeriodStart();
        entity.concertPeriodEnd = concert.getConcertPeriodEnd();
        entity.isActive = concert.isActive();
        entity.createdAt = concert.getCreatedAt();
        entity.updatedAt = concert.getLastUpdatedAt();
        return entity;
    }

    // Getters and Setters
    public String getConcertId() {
        return concertId;
    }
    
    public void setConcertId(String concertId) {
        this.concertId = concertId;
    }

    public String getConcertName() {
        return concertName;
    }
    
    public void setConcertName(String concertName) {
        this.concertName = concertName;
    }

    public LocalDate getConcertPeriodStart() {
        return concertPeriodStart;
    }
    
    public void setConcertPeriodStart(LocalDate concertPeriodStart) {
        this.concertPeriodStart = concertPeriodStart;
    }

    public LocalDate getConcertPeriodEnd() {
        return concertPeriodEnd;
    }
    
    public void setConcertPeriodEnd(LocalDate concertPeriodEnd) {
        this.concertPeriodEnd = concertPeriodEnd;
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
