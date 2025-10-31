package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.infrastructure.entity.SeatEntity;
import kr.hhplus.be.server.infrastructure.entity.SeatReservationStatusEntity;
import kr.hhplus.be.server.infrastructure.repository.JpaSeatRepository;
import kr.hhplus.be.server.infrastructure.repository.JpaSeatReservationStatusRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 좌석 데이터 저장/조회 어댑터
 * 
 * SeatPort 인터페이스를 구현해서 도메인 레이어와 데이터베이스를 연결해.
 * 클린아키텍처의 인프라스트럭처 레이어에서 포트를 구현하는 역할을 해.
 * SeatReservationStatusEntity를 사용하여 좌석 상태를 관리합니다.
 */
@Primary
@Repository
@Transactional
public class JpaSeatAdapter implements SeatPort {
    private final JpaSeatRepository jpaSeatRepository;
    private final JpaSeatReservationStatusRepository jpaSeatReservationStatusRepository;

    /**
     * JPA Repository를 주입받아 사용해.
     * 도메인 레이어는 저장소에 직접 의존하지 않고, 이 어댑터를 통해 간접적으로 접근해.
     */
    public JpaSeatAdapter(JpaSeatRepository jpaSeatRepository, 
                         JpaSeatReservationStatusRepository jpaSeatReservationStatusRepository) {
        this.jpaSeatRepository = jpaSeatRepository;
        this.jpaSeatReservationStatusRepository = jpaSeatReservationStatusRepository;
    }

    @Override
    public boolean isSeatAvailable(String seatId) {
        // 좌석이 존재하고 활성화되어 있는지 확인
        Optional<SeatEntity> seatOptional = jpaSeatRepository.findById(seatId);
        if (seatOptional.isEmpty() || !seatOptional.get().isActive()) {
            return false;
        }
        
        // 좌석 예약 상태 확인 (기본적으로 performanceId는 "default"로 설정)
        String performanceId = "default";
        SeatReservationStatusEntity statusEntity = jpaSeatReservationStatusRepository
            .findBySeatIdAndPerformanceId(seatId, performanceId);
        
        // 상태 엔티티가 없거나 AVAILABLE 상태인 경우 예약 가능
        if (statusEntity == null) {
            return true;
        }
        
        // HOLD 상태이지만 만료된 경우 AVAILABLE로 변경
        if ("HOLD".equals(statusEntity.getSeatStatus()) && 
            statusEntity.getHoldExpiresAt() != null && 
            statusEntity.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            
            // 만료된 HOLD 상태를 AVAILABLE로 변경
            jpaSeatReservationStatusRepository.updateSeatStatus(
                seatId, performanceId, "AVAILABLE", null, null);
            return true;
        }
        
        return "AVAILABLE".equals(statusEntity.getSeatStatus());
    }

    @Override
    @Transactional
    public void holdSeat(String seatId, String reservationId) {
        // 좌석이 예약 가능한지 확인
        if (!isSeatAvailable(seatId)) {
            throw new IllegalStateException("좌석을 예약할 수 없습니다. 이미 예약되었거나 비활성화된 좌석입니다.");
        }
        
        String performanceId = "default";
        LocalDateTime holdExpiresAt = LocalDateTime.now().plusMinutes(5);
        
        // 좌석 예약 상태 엔티티 생성 또는 업데이트
        SeatReservationStatusEntity statusEntity = jpaSeatReservationStatusRepository
            .findBySeatIdAndPerformanceId(seatId, performanceId);
        
        if (statusEntity == null) {
            // 새로운 상태 엔티티 생성
            statusEntity = new SeatReservationStatusEntity();
            statusEntity.setStatusId(UUID.randomUUID().toString());
            statusEntity.setSeatId(seatId);
            statusEntity.setPerformanceId(performanceId);
            statusEntity.setSeatStatus("HOLD");
            statusEntity.setHeldByReservationId(reservationId);
            statusEntity.setHoldExpiresAt(holdExpiresAt);
            statusEntity.setCreatedAt(LocalDateTime.now());
            statusEntity.setUpdatedAt(LocalDateTime.now());
        } else {
            // 기존 상태 엔티티 업데이트
            statusEntity.setSeatStatus("HOLD");
            statusEntity.setHeldByReservationId(reservationId);
            statusEntity.setHoldExpiresAt(holdExpiresAt);
            statusEntity.setUpdatedAt(LocalDateTime.now());
        }
        
        jpaSeatReservationStatusRepository.save(statusEntity);
    }

    @Override
    @Transactional
    public void sellSeat(String seatId) {
        String performanceId = "default";
        
        // 좌석 예약 상태를 SOLD로 변경
        int updatedRows = jpaSeatReservationStatusRepository.updateSeatStatus(
            seatId, performanceId, "SOLD", null, null);
        
        if (updatedRows == 0) {
            throw new IllegalStateException("좌석을 판매할 수 없습니다. 좌석이 존재하지 않거나 상태가 올바르지 않습니다.");
        }
    }

    @Override
    @Transactional
    public void releaseSeat(String seatId) {
        String performanceId = "default";
        
        // 좌석 예약 상태를 AVAILABLE로 변경
        int updatedRows = jpaSeatReservationStatusRepository.updateSeatStatus(
            seatId, performanceId, "AVAILABLE", null, null);
        
        if (updatedRows == 0) {
            throw new IllegalStateException("좌석을 해제할 수 없습니다. 좌석이 존재하지 않거나 상태가 올바르지 않습니다.");
        }
    }
}
