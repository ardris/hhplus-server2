package kr.hhplus.be.server.dto.response;

/**
 * 공연장 응답 DTO
 */
public class VenueResponse {
    private String venueId;
    private String venueName;
    private String venueAddress;
    private String venueCity;
    private int venueCapacity;
    private String venueDescription;
    private boolean isActive;

    public VenueResponse() {}

    public VenueResponse(String venueId, String venueName, String venueAddress, 
                        String venueCity, int venueCapacity, String venueDescription, 
                        boolean isActive) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueCity = venueCity;
        this.venueCapacity = venueCapacity;
        this.venueDescription = venueDescription;
        this.isActive = isActive;
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

    public String getVenueAddress() {
        return venueAddress;
    }
    
    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getVenueCity() {
        return venueCity;
    }
    
    public void setVenueCity(String venueCity) {
        this.venueCity = venueCity;
    }

    public int getVenueCapacity() {
        return venueCapacity;
    }
    
    public void setVenueCapacity(int venueCapacity) {
        this.venueCapacity = venueCapacity;
    }

    public String getVenueDescription() {
        return venueDescription;
    }
    
    public void setVenueDescription(String venueDescription) {
        this.venueDescription = venueDescription;
    }

    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
}