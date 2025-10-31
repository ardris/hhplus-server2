package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 공연장 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "venue_info")
public class VenueEntity {
    
    @Id
    @Column(name = "venue_id")
    private String venueId;
    
    @Column(name = "venue_name", nullable = false)
    private String venueName;
    
    @Column(name = "venue_address", nullable = false)
    private String venueAddress;
    
    @Column(name = "venue_city", nullable = false)
    private String venueCity;
    
    @Column(name = "venue_capacity", nullable = false)
    private Integer venueCapacity;
    
    @Column(name = "venue_description")
    private String venueDescription;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Domain Model로 변환
     */
    public kr.hhplus.be.server.model.Venue toDomain() {
        return new kr.hhplus.be.server.model.Venue(
            this.venueId,
            this.venueName,
            this.venueAddress,
            this.venueCity,
            this.venueCapacity,
            this.venueDescription,
            this.isActive,
            this.createdAt,
            this.updatedAt
        );
    }
    
    /**
     * Domain Model로부터 생성
     */
    public static VenueEntity fromDomain(kr.hhplus.be.server.model.Venue venue) {
        VenueEntity entity = new VenueEntity();
        entity.venueId = venue.getVenueId();
        entity.venueName = venue.getVenueName();
        entity.venueAddress = venue.getVenueAddress();
        entity.venueCity = venue.getVenueCity();
        entity.venueCapacity = venue.getVenueCapacity();
        entity.venueDescription = venue.getVenueDescription();
        entity.isActive = venue.isActive();
        entity.createdAt = venue.getCreatedAt();
        entity.updatedAt = venue.getLastUpdatedAt();
        return entity;
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
}
