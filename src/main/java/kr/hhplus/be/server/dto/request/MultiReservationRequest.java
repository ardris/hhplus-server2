package kr.hhplus.be.server.dto.request;

import java.math.BigDecimal;
import java.util.List;

/**
 * 다중 좌석 예약 요청 DTO
 */
public class MultiReservationRequest {
    private String userId;
    private String performanceId;
    private List<String> seatIds;
    private BigDecimal totalPrice;

    public MultiReservationRequest() {
    }

    public MultiReservationRequest(String userId, String performanceId, List<String> seatIds, BigDecimal totalPrice) {
        this.userId = userId;
        this.performanceId = performanceId;
        this.seatIds = seatIds;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPerformanceId() {
        return performanceId;
    }

    public void setPerformanceId(String performanceId) {
        this.performanceId = performanceId;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<String> seatIds) {
        this.seatIds = seatIds;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
