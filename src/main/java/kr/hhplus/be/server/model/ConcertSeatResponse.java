package kr.hhplus.be.server.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 콘서트 + 좌석 정보를 함께 조회할 때 사용
 */
public class ConcertSeatResponse {
    private ConcertInfo concertInfo;
    private List<SeatResponse> availableSeats;
    private int totalSeats;
    private int availableCount;
    
    public ConcertSeatResponse() {}
    
    public ConcertSeatResponse(ConcertInfo concertInfo, List<SeatResponse> availableSeats, 
                              int totalSeats, int availableCount) {
        this.concertInfo = concertInfo;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
        this.availableCount = availableCount;
    }
    
    public ConcertInfo getConcertInfo() {
        return concertInfo;
    }
    
    public void setConcertInfo(ConcertInfo concertInfo) {
        this.concertInfo = concertInfo;
    }
    
    public List<SeatResponse> getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(List<SeatResponse> availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public int getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
    
    public int getAvailableCount() {
        return availableCount;
    }
    
    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }
    
    /**
     * 콘서트 정보 내부 클래스
     */
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
}
