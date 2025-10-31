package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.ConcertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

/**
 * 콘서트 JPA Repository
 */
@Repository
public interface JpaConcertRepository extends JpaRepository<ConcertEntity, String> {
    
    @Query("SELECT c FROM ConcertEntity c WHERE c.isActive = true")
    List<ConcertEntity> findActiveConcerts();
    
    @Query("SELECT c FROM ConcertEntity c WHERE c.concertPeriodStart <= :date AND c.concertPeriodEnd >= :date")
    List<ConcertEntity> findConcertsByDate(@Param("date") LocalDate date);
    
    /**
     * 날짜와 제목으로 콘서트 조회
     */
    @Query("SELECT c FROM ConcertEntity c WHERE c.concertName = :title AND c.concertPeriodStart <= :date AND c.concertPeriodEnd >= :date")
    Optional<ConcertEntity> findByDateAndTitle(@Param("date") String date, @Param("title") String title);
}
