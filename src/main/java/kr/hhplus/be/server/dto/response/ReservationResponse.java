package kr.hhplus.be.server.dto.response;

import java.math.BigDecimal;

public class ReservationResponse {
    private String reservationId;
    private ConcertInfo concertInfo;
    private SeatInfo seatInfo;
    private String holdExpiresAt;
    private String status;

    public ReservationResponse() {}

    public ReservationResponse(String reservationId, ConcertInfo concertInfo, 
                             SeatInfo seatInfo, String holdExpiresAt, String status) {
        this.reservationId = reservationId;
        this.concertInfo = concertInfo;
        this.seatInfo = seatInfo;
        this.holdExpiresAt = holdExpiresAt;
        this.status = status;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public ConcertInfo getConcertInfo() {
        return concertInfo;
    }

    public void setConcertInfo(ConcertInfo concertInfo) {
        this.concertInfo = concertInfo;
    }

    public SeatInfo getSeatInfo() {
        return seatInfo;
    }

    public void setSeatInfo(SeatInfo seatInfo) {
        this.seatInfo = seatInfo;
    }

    public String getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(String holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class ConcertInfo {
        private String date;
        private String title;
        private BigDecimal ticketPrice;

        public ConcertInfo() {}

        public ConcertInfo(String date, String title, BigDecimal ticketPrice) {
            this.date = date;
            this.title = title;
            this.ticketPrice = ticketPrice;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public BigDecimal getTicketPrice() {
            return ticketPrice;
        }

        public void setTicketPrice(BigDecimal ticketPrice) {
            this.ticketPrice = ticketPrice;
        }
    }

    public static class SeatInfo {
        private String seatId;
        private int seatNumber;

        public SeatInfo() {}

        public SeatInfo(String seatId, int seatNumber) {
            this.seatId = seatId;
            this.seatNumber = seatNumber;
        }

        public String getSeatId() {
            return seatId;
        }

        public void setSeatId(String seatId) {
            this.seatId = seatId;
        }

        public int getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(int seatNumber) {
            this.seatNumber = seatNumber;
        }
    }
}
