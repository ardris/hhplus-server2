package kr.hhplus.be.server.model;

import kr.hhplus.be.server.dto.response.VenueInfo;
import java.math.BigDecimal;
import java.util.List;

/**
 * 콘서트 정보 응답 모델
 * 콘서트 목록 조회 시 사용
 */
public class ConcertResponse {
    private String concertId;
    private String concertName;
    private String concertPeriodStart;
    private String concertPeriodEnd;
    private boolean isActive;
    private List<VenueInfo> venues;

    public ConcertResponse() {
    }

    public ConcertResponse(String concertId, String concertName, String concertPeriodStart,
            String concertPeriodEnd, boolean isActive, List<VenueInfo> venues) {
        this.concertId = concertId;
        this.concertName = concertName;
        this.concertPeriodStart = concertPeriodStart;
        this.concertPeriodEnd = concertPeriodEnd;
        this.isActive = isActive;
        this.venues = venues;
    }

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

    public String getConcertPeriodStart() {
        return concertPeriodStart;
    }

    public void setConcertPeriodStart(String concertPeriodStart) {
        this.concertPeriodStart = concertPeriodStart;
    }

    public String getConcertPeriodEnd() {
        return concertPeriodEnd;
    }

    public void setConcertPeriodEnd(String concertPeriodEnd) {
        this.concertPeriodEnd = concertPeriodEnd;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<VenueInfo> getVenues() {
        return venues;
    }

    public void setVenues(List<VenueInfo> venues) {
        this.venues = venues;
    }
}
