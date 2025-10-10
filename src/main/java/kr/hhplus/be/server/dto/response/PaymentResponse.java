package kr.hhplus.be.server.dto.response;

import java.math.BigDecimal;

public class PaymentResponse {
    private String paymentId;
    private ReservationInfo reservationInfo;
    private PaymentInfo paymentInfo;
    private UserInfo userInfo;

    public PaymentResponse() {
    }

    public PaymentResponse(String paymentId, ReservationInfo reservationInfo,
            PaymentInfo paymentInfo, UserInfo userInfo) {
        this.paymentId = paymentId;
        this.reservationInfo = reservationInfo;
        this.paymentInfo = paymentInfo;
        this.userInfo = userInfo;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public ReservationInfo getReservationInfo() {
        return reservationInfo;
    }

    public void setReservationInfo(ReservationInfo reservationInfo) {
        this.reservationInfo = reservationInfo;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static class ReservationInfo {
        private String reservationId;
        private String concertDate;
        private String concertTitle;
        private String venueName;
        private String venueCity;
        private int seatNumber;

        public ReservationInfo() {
        }

        public ReservationInfo(String reservationId, String concertDate,
                String concertTitle, String venueName, String venueCity, int seatNumber) {
            this.reservationId = reservationId;
            this.concertDate = concertDate;
            this.concertTitle = concertTitle;
            this.venueName = venueName;
            this.venueCity = venueCity;
            this.seatNumber = seatNumber;
        }

        public String getReservationId() {
            return reservationId;
        }

        public void setReservationId(String reservationId) {
            this.reservationId = reservationId;
        }

        public String getConcertDate() {
            return concertDate;
        }

        public void setConcertDate(String concertDate) {
            this.concertDate = concertDate;
        }

        public String getConcertTitle() {
            return concertTitle;
        }

        public void setConcertTitle(String concertTitle) {
            this.concertTitle = concertTitle;
        }

        public String getVenueName() {
            return venueName;
        }

        public void setVenueName(String venueName) {
            this.venueName = venueName;
        }

        public String getVenueCity() {
            return venueCity;
        }

        public void setVenueCity(String venueCity) {
            this.venueCity = venueCity;
        }

        public int getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(int seatNumber) {
            this.seatNumber = seatNumber;
        }
    }

    public static class PaymentInfo {
        private BigDecimal amount;
        private String status;
        private String paidAt;

        public PaymentInfo() {
        }

        public PaymentInfo(BigDecimal amount, String status, String paidAt) {
            this.amount = amount;
            this.status = status;
            this.paidAt = paidAt;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(String paidAt) {
            this.paidAt = paidAt;
        }
    }

    public static class UserInfo {
        private String userId;
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;

        public UserInfo() {
        }

        public UserInfo(String userId, BigDecimal balanceBefore, BigDecimal balanceAfter) {
            this.userId = userId;
            this.balanceBefore = balanceBefore;
            this.balanceAfter = balanceAfter;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public BigDecimal getBalanceBefore() {
            return balanceBefore;
        }

        public void setBalanceBefore(BigDecimal balanceBefore) {
            this.balanceBefore = balanceBefore;
        }

        public BigDecimal getBalanceAfter() {
            return balanceAfter;
        }

        public void setBalanceAfter(BigDecimal balanceAfter) {
            this.balanceAfter = balanceAfter;
        }
    }
}
