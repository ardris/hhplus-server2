package kr.hhplus.be.server.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 다중 좌석 예약 응답 아직 미구현...
 */
public class MultiReservationResponse {
    private String reservationGroupId;
    private List<ReservationResponse> reservations;
    private LocalDateTime holdExpiresAt;
    private String status;

    public MultiReservationResponse() {
    }

    public MultiReservationResponse(String reservationGroupId, List<ReservationResponse> reservations,
            LocalDateTime holdExpiresAt, String status) {
        this.reservationGroupId = reservationGroupId;
        this.reservations = reservations;
        this.holdExpiresAt = holdExpiresAt;
        this.status = status;
    }

    // Getters and Setters
    public String getReservationGroupId() {
        return reservationGroupId;
    }

    public void setReservationGroupId(String reservationGroupId) {
        this.reservationGroupId = reservationGroupId;
    }

    public List<ReservationResponse> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationResponse> reservations) {
        this.reservations = reservations;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
