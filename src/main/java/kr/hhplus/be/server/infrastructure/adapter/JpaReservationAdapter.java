package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.repository.ReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 예약 데이터 저장/조회 어댑터
 * 
 * ReservationRepositoryPort 인터페이스를 구현해서 도메인 레이어와 데이터베이스를 연결해.
 * 클린아키텍처의 인프라스트럭처 레이어에서 포트를 구현하는 역할을 해.
 */
@Repository
public class JpaReservationAdapter implements ReservationRepositoryPort {
    private final ReservationRepository reservationRepository;

    /**
     * 기존 저장소를 주입받아 사용해.
     * 도메인 레이어는 저장소에 직접 의존하지 않고, 이 어댑터를 통해 간접적으로 접근해.
     */
    public JpaReservationAdapter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        // 도메인 엔티티를 기존 모델로 변환하여 저장
        kr.hhplus.be.server.model.Reservation reservationModel = convertToModel(reservation);
        kr.hhplus.be.server.model.Reservation savedReservationModel = reservationRepository.save(reservationModel);

        // 저장된 모델을 다시 도메인 엔티티로 변환하여 반환
        return convertToDomain(savedReservationModel);
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        Optional<kr.hhplus.be.server.model.Reservation> reservationModelOptional = reservationRepository
                .findById(reservationId);
        if (reservationModelOptional.isPresent()) {
            return Optional.of(convertToDomain(reservationModelOptional.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findByUserId(String userId) {
        List<kr.hhplus.be.server.model.Reservation> reservationModelList = reservationRepository.findByUserId(userId);
        List<Reservation> reservationDomainList = new ArrayList<>();
        for (kr.hhplus.be.server.model.Reservation reservationModel : reservationModelList) {
            reservationDomainList.add(convertToDomain(reservationModel));
        }
        return reservationDomainList;
    }

    @Override
    public List<Reservation> findByConcertId(String concertId) {
        List<kr.hhplus.be.server.model.Reservation> reservationModelList = reservationRepository
                .findByConcertId(concertId);
        List<Reservation> reservationDomainList = new ArrayList<>();
        for (kr.hhplus.be.server.model.Reservation reservationModel : reservationModelList) {
            reservationDomainList.add(convertToDomain(reservationModel));
        }
        return reservationDomainList;
    }

    @Override
    public void deleteById(String reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    /**
     * 도메인 엔티티를 기존 모델로 변환
     */
    private kr.hhplus.be.server.model.Reservation convertToModel(Reservation domain) {
        // TODO: 도메인 엔티티를 기존 모델로 변환하는 로직 구현
        return new kr.hhplus.be.server.model.Reservation();
    }

    /**
     * 기존 모델을 도메인 엔티티로 변환
     */
    private Reservation convertToDomain(kr.hhplus.be.server.model.Reservation reservationModel) {
        // TODO: 기존 모델을 도메인 엔티티로 변환하는 로직 구현
        return Reservation.create(
                reservationModel.getUserId(),
                reservationModel.getConcertId(),
                reservationModel.getSeatId(),
                reservationModel.getTicketPrice());
    }
}
