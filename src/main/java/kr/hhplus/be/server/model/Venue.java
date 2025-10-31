package kr.hhplus.be.server.model;

import java.time.LocalDateTime;

/**
 * 공연장 정보 모델
 */
public class Venue {
    private String venueId;
    private String venueName;
    private String venueAddress;
    private String venueCity;
    private int venueCapacity;
    private String venueDescription;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Venue() {
    }

    public Venue(String venueId, String venueName, String venueAddress, String venueCity,
            int venueCapacity, String venueDescription, boolean isActive) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueCity = venueCity;
        this.venueCapacity = venueCapacity;
        this.venueDescription = venueDescription;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 전체 생성자
    public Venue(String venueId, String venueName, String venueAddress, String venueCity,
            int venueCapacity, String venueDescription, boolean isActive,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueCity = venueCity;
        this.venueCapacity = venueCapacity;
        this.venueDescription = venueDescription;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return updatedAt;
    }
}
