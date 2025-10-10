package kr.hhplus.be.server.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 콘서트 응답에 포함될 공연 정보 DTO
 */
public class PerformanceInfo {
    private String performanceId;
    private LocalDate performanceDate;
    private LocalTime performanceTime;
    private BigDecimal ticketPrice;
    private int availableSeats;

    public PerformanceInfo() {
    }

    public PerformanceInfo(String performanceId, LocalDate performanceDate, LocalTime performanceTime,
            BigDecimal ticketPrice, int availableSeats) {
        this.performanceId = performanceId;
        this.performanceDate = performanceDate;
        this.performanceTime = performanceTime;
        this.ticketPrice = ticketPrice;
        this.availableSeats = availableSeats;
    }

    // Getters and Setters
    public String getPerformanceId() {
        return performanceId;
    }

    public void setPerformanceId(String performanceId) {
        this.performanceId = performanceId;
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

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
