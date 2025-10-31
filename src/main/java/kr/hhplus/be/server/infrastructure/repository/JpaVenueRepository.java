package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.VenueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 공연장 JPA Repository
 */
@Repository
public interface JpaVenueRepository extends JpaRepository<VenueEntity, String> {
    
    @Query("SELECT v FROM VenueEntity v WHERE v.isActive = true")
    List<VenueEntity> findActiveVenues();
    
    @Query("SELECT v FROM VenueEntity v WHERE v.venueCity = :city")
    List<VenueEntity> findByCity(@Param("city") String city);
}
