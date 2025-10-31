package kr.hhplus.be.server.model;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * 콘서트 정보를 관리하는 도메인 모델 (Performance 기반으로 확장)
 * 
 * 콘서트 예약 서비스에서 콘서트의 기본 정보를 관리하는 클래스입니다.
 * 이제 Performance와 Venue를 통해 실제 공연 정보를 관리합니다.
 */
public class Concert {
    
    private String concertId;
    private String concertName;
    private LocalDate concertPeriodStart;
    private LocalDate concertPeriodEnd;
    private boolean isActive;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    
    public Concert(String concertId, String concertName, LocalDate concertPeriodStart, LocalDate concertPeriodEnd) {
        this.concertId = concertId;
        this.concertName = concertName;
        this.concertPeriodStart = concertPeriodStart;
        this.concertPeriodEnd = concertPeriodEnd;
        this.isActive = true;
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    public Concert(String concertId, String concertName, LocalDate concertPeriodStart, LocalDate concertPeriodEnd, 
                  boolean isActive, java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
        this.concertId = concertId;
        this.concertName = concertName;
        this.concertPeriodStart = concertPeriodStart;
        this.concertPeriodEnd = concertPeriodEnd;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 콘서트 기간이 예약 가능한지 확인합니다.
     * 
     * @return true: 예약 가능 (오늘 이후), false: 예약 불가 (과거 날짜)
     * 
     * 이유: 사용자가 예약 가능한 콘서트 목록을 조회할 때 사용됩니다.
     * 과거 날짜의 콘서트는 예약할 수 없으므로, 오늘 날짜 이후의 콘서트만 예약 가능합니다.
     * 오늘 날짜도 포함하여 당일 예약도 가능하도록 했습니다.
     */
    // 콘서트 기간 예약 가능 여부 확인
    public boolean isConcertPeriodAvailable() {
        return concertPeriodEnd.isAfter(LocalDate.now()) || concertPeriodEnd.isEqual(LocalDate.now());
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
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.time.LocalDateTime getLastUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getConcertDate() {
        return concertPeriodStart;
    }

    public boolean isConcertDateAvailable() {
        return LocalDate.now().isBefore(concertPeriodStart);
    }

    public String getDate() {
        return concertPeriodStart.toString();
    }

    public String getTitle() {
        return concertName;
    }

    public BigDecimal getTicketPrice() {
        return BigDecimal.ZERO; // 기본값, 실제로는 Performance에서 가져와야 함
    }
}
