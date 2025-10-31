package kr.hhplus.be.server.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 콘서트 응답 DTO
 */
public class ConcertResponse {
    private String concertId;
    private String concertName;
    private LocalDate concertDate;
    private String venueName;
    private String venueCity;
    private BigDecimal ticketPrice;
    private int availableSeats;
    private boolean isActive;

    public ConcertResponse() {}

    public ConcertResponse(String concertId, String concertName, LocalDate concertDate, 
                          String venueName, String venueCity, BigDecimal ticketPrice, 
                          int availableSeats, boolean isActive) {
        this.concertId = concertId;
        this.concertName = concertName;
        this.concertDate = concertDate;
        this.venueName = venueName;
        this.venueCity = venueCity;
        this.ticketPrice = ticketPrice;
        this.availableSeats = availableSeats;
        this.isActive = isActive;
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

    public LocalDate getConcertDate() {
        return concertDate;
    }
    
    public void setConcertDate(LocalDate concertDate) {
        this.concertDate = concertDate;
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

    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}
