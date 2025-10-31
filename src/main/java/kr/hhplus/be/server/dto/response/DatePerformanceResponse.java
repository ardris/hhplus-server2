package kr.hhplus.be.server.dto.response;

import java.util.List;

/**
 * 날짜별 공연 정보 응답 DTO
 */
public class DatePerformanceResponse {
    private String date;
    private List<PerformanceInfo> performances;

    public DatePerformanceResponse() {}

    public DatePerformanceResponse(String date, List<PerformanceInfo> performances) {
        this.date = date;
        this.performances = performances;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }

    public List<PerformanceInfo> getPerformances() {
        return performances;
    }
    
    public void setPerformances(List<PerformanceInfo> performances) {
        this.performances = performances;
    }

    /**
     * 공연 정보 내부 클래스
     */
    public static class PerformanceInfo {
        private String performanceId;
        private String concertName;
        private String venueName;
        private String venueCity;
        private String performanceTime;
        private java.math.BigDecimal ticketPrice;

        public PerformanceInfo() {}

        public PerformanceInfo(String performanceId, String concertName, String venueName, 
                             String venueCity, String performanceTime, java.math.BigDecimal ticketPrice) {
            this.performanceId = performanceId;
            this.concertName = concertName;
            this.venueName = venueName;
            this.venueCity = venueCity;
            this.performanceTime = performanceTime;
            this.ticketPrice = ticketPrice;
        }

        // Getters and Setters
        public String getPerformanceId() {
            return performanceId;
        }
        
        public void setPerformanceId(String performanceId) {
            this.performanceId = performanceId;
        }

        public String getConcertName() {
            return concertName;
        }
        
        public void setConcertName(String concertName) {
            this.concertName = concertName;
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

        public String getPerformanceTime() {
            return performanceTime;
        }
        
        public void setPerformanceTime(String performanceTime) {
            this.performanceTime = performanceTime;
        }

        public java.math.BigDecimal getTicketPrice() {
            return ticketPrice;
        }
        
        public void setTicketPrice(java.math.BigDecimal ticketPrice) {
            this.ticketPrice = ticketPrice;
        }
    }
}