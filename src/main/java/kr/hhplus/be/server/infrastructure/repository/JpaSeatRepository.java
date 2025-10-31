package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 좌석 JPA Repository
 * 
 * Infrastructure Layer에서 데이터베이스와 직접 소통하는 Repository입니다.
 * JPA를 사용하여 CRUD 작업을 수행합니다.
 * 
 * 참고: SeatEntity는 물리적 좌석 정보만 담고 있습니다.
 * 좌석의 예약 상태는 SeatReservationStatusEntity에서 관리됩니다.
 */
@Repository
public interface JpaSeatRepository extends JpaRepository<SeatEntity, String> {
    
    // 기본 CRUD 메서드만 사용
    // findById, save, delete 등은 JpaRepository에서 상속됨
    
    /**
     * 공연장 ID로 좌석 조회
     */
    List<SeatEntity> findByVenueId(String venueId);
    
    /**
     * 활성화된 좌석만 조회
     */
    List<SeatEntity> findByIsActiveTrue();
}