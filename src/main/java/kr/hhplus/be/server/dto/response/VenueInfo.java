package kr.hhplus.be.server.dto.response;

import java.util.List;

/**
 * 콘서트 응답에 포함될 공연장 정보 DTO
 */
public class VenueInfo {
    private String venueId;
    private String venueName;
    private String venueCity;
    private String venueAddress;
    private int venueCapacity;
    private List<PerformanceInfo> performances;

    public VenueInfo() {
    }

    public VenueInfo(String venueId, String venueName, String venueCity, String venueAddress,
            int venueCapacity, List<PerformanceInfo> performances) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.venueCity = venueCity;
        this.venueAddress = venueAddress;
        this.venueCapacity = venueCapacity;
        this.performances = performances;
    }

    // Getters and Setters
    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
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

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public int getVenueCapacity() {
        return venueCapacity;
    }

    public void setVenueCapacity(int venueCapacity) {
        this.venueCapacity = venueCapacity;
    }

    public List<PerformanceInfo> getPerformances() {
        return performances;
    }

    public void setPerformances(List<PerformanceInfo> performances) {
        this.performances = performances;
    }
}
