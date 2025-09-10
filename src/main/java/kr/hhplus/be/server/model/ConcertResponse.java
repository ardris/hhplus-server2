package kr.hhplus.be.server.model;

import java.math.BigDecimal;

/**
 * 콘서트 정보 응답 모델
 * 콘서트 목록 조회 시 사용
 */
public class ConcertResponse {
    private String date;
    private String title;
    private BigDecimal ticketPrice;
    
    public ConcertResponse() {}
    
    public ConcertResponse(String date, String title, BigDecimal ticketPrice) {
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
