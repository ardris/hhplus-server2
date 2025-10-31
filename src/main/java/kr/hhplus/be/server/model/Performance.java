package kr.hhplus.be.server.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 공연 정보 모델
 */
public class Performance {
    private String performanceId;
    private String concertId;
    private String venueId;
    private LocalDate performanceDate;
    private LocalTime performanceTime;
    private BigDecimal ticketPrice;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Performance() {}

    public Performance(String performanceId, String concertId, String venueId, 
                      LocalDate performanceDate, LocalTime performanceTime, 
                      BigDecimal ticketPrice, boolean isActive) {
        this.performanceId = performanceId;
        this.concertId = concertId;
        this.venueId = venueId;
        this.performanceDate = performanceDate;
        this.performanceTime = performanceTime;
        this.ticketPrice = ticketPrice;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 전체 생성자
    public Performance(String performanceId, String concertId, String venueId, 
                      LocalDate performanceDate, LocalTime performanceTime, 
                      BigDecimal ticketPrice, boolean isActive, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.performanceId = performanceId;
        this.concertId = concertId;
        this.venueId = venueId;
        this.performanceDate = performanceDate;
        this.performanceTime = performanceTime;
        this.ticketPrice = ticketPrice;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }
    
    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
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

    public LocalDateTime getLastUpdatedAt() {
        return updatedAt;
    }
}