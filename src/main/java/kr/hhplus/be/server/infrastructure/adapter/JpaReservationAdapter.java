package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.infrastructure.entity.ReservationEntity;
import kr.hhplus.be.server.infrastructure.repository.JpaReservationRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 예약 데이터 저장/조회 어댑터
 * 
 * ReservationRepositoryPort 인터페이스를 구현해서 도메인 레이어와 데이터베이스를 연결해.
 * 클린아키텍처의 인프라스트럭처 레이어에서 포트를 구현하는 역할을 해.
 */
@Primary
@Repository
@Transactional
public class JpaReservationAdapter implements ReservationRepositoryPort {
    private final JpaReservationRepository jpaReservationRepository;

    /**
     * JPA Repository를 주입받아 사용해.
     * 도메인 레이어는 저장소에 직접 의존하지 않고, 이 어댑터를 통해 간접적으로 접근해.
     */
    public JpaReservationAdapter(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        // 도메인 엔티티를 JPA 엔티티로 변환하여 저장
        ReservationEntity entity = ReservationEntity.fromDomain(reservation);
        ReservationEntity savedEntity = jpaReservationRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        Optional<ReservationEntity> entity = jpaReservationRepository.findById(reservationId);
        return entity.map(ReservationEntity::toDomain);
    }

    @Override
    public List<Reservation> findByUserId(String userId) {
        List<ReservationEntity> entities = jpaReservationRepository.findByUserId(userId);
        List<Reservation> reservations = new ArrayList<>();
        for (ReservationEntity entity : entities) {
            reservations.add(entity.toDomain());
        }
        return reservations;
    }

    @Override
    public List<Reservation> findByConcertId(String concertId) {
        List<ReservationEntity> entities = jpaReservationRepository.findByPerformanceId(concertId);
        List<Reservation> reservations = new ArrayList<>();
        for (ReservationEntity entity : entities) {
            reservations.add(entity.toDomain());
        }
        return reservations;
    }

    @Override
    public void deleteById(String reservationId) {
        jpaReservationRepository.deleteById(reservationId);
    }
}