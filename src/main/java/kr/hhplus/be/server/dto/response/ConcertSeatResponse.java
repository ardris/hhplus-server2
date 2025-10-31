package kr.hhplus.be.server.dto.response;

import java.util.List;

/**
 * 콘서트 좌석 정보 응답 DTO
 */
public class ConcertSeatResponse {
    private String concertId;
    private String concertName;
    private String concertDate;
    private String venueName;
    private int totalSeats;
    private int availableSeats;
    private List<SeatInfo> seats;

    public ConcertSeatResponse() {}

    public ConcertSeatResponse(String concertId, String concertName, String concertDate, 
                              String venueName, int totalSeats, int availableSeats, 
                              List<SeatInfo> seats) {
        this.concertId = concertId;
        this.concertName = concertName;
        this.concertDate = concertDate;
        this.venueName = venueName;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.seats = seats;
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

    public String getConcertDate() {
        return concertDate;
    }
    
    public void setConcertDate(String concertDate) {
        this.concertDate = concertDate;
    }

    public String getVenueName() {
        return venueName;
    }
    
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public int getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public List<SeatInfo> getSeats() {
        return seats;
    }
    
    public void setSeats(List<SeatInfo> seats) {
        this.seats = seats;
    }

    /**
     * 좌석 정보 내부 클래스
     */
    public static class SeatInfo {
        private String seatId;
        private String seatNumber;
        private String seatStatus;
        private java.math.BigDecimal price;

        public SeatInfo() {}

        public SeatInfo(String seatId, String seatNumber, String seatStatus, java.math.BigDecimal price) {
            this.seatId = seatId;
            this.seatNumber = seatNumber;
            this.seatStatus = seatStatus;
            this.price = price;
        }

        // Getters and Setters
        public String getSeatId() {
            return seatId;
        }
        
        public void setSeatId(String seatId) {
            this.seatId = seatId;
        }

        public String getSeatNumber() {
            return seatNumber;
        }
        
        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }

        public String getSeatStatus() {
            return seatStatus;
        }
        
        public void setSeatStatus(String seatStatus) {
            this.seatStatus = seatStatus;
        }

        public java.math.BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(java.math.BigDecimal price) {
            this.price = price;
        }
    }
}
