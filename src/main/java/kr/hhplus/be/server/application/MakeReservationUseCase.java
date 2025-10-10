package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.domain.port.UserPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 예약 생성 Use Case
 * 
 * 클린아키텍처의 애플리케이션 레이어에서 예약 생성 비즈니스 로직을 처리해.
 * 도메인 엔티티와 포트를 사용해서 외부 의존성 없이 순수한 비즈니스 로직만 구현했어.
 */
@Service
public class MakeReservationUseCase {
    private final ReservationRepositoryPort reservationRepository;
    private final SeatPort seatPort;
    private final UserPort userPort;

    /**
     * 의존성 주입
     * 포트 인터페이스에만 의존해서 구현체와 분리했어. 테스트할 때 Mock으로 쉽게 대체할 수 있어.
     */
    public MakeReservationUseCase(ReservationRepositoryPort reservationRepository,
            SeatPort seatPort,
            UserPort userPort) {
        this.reservationRepository = reservationRepository;
        this.seatPort = seatPort;
        this.userPort = userPort;
    }

    /**
     * 예약 생성 비즈니스 시나리오
     * 
     * 시나리오:
     * 1. 사용자 존재 확인
     * 2. 좌석 가용성 확인
     * 3. 예약 생성
     * 4. 좌석 임시 배정
     * 5. 예약 저장
     */
    public Reservation execute(MakeReservationCommand command) {
        // 1. 사용자 존재 확인
        if (userPort.findById(command.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 2. 좌석 가용성 확인
        if (!seatPort.isSeatAvailable(command.getSeatId())) {
            throw new IllegalStateException("좌석을 예약할 수 없습니다.");
        }

        // 3. 예약 생성 (도메인 엔티티의 팩토리 메서드 사용)
        Reservation reservation = Reservation.create(
                command.getUserId(),
                command.getConcertId(),
                command.getSeatId(),
                command.getTicketPrice());

        // 4. 좌석 임시 배정 (포트를 통해 외부 시스템 호출)
        seatPort.holdSeat(command.getSeatId(), reservation.getReservationId());

        // 5. 예약 저장 (포트를 통해 외부 시스템 호출)
        return reservationRepository.save(reservation);
    }
}

/**
 * 예약 생성 명령 객체
 */
class MakeReservationCommand {
    private String userId;
    private String concertId;
    private String seatId;
    private BigDecimal ticketPrice;

    public MakeReservationCommand(String userId, String concertId, String seatId, BigDecimal ticketPrice) {
        this.userId = userId;
        this.concertId = concertId;
        this.seatId = seatId;
        this.ticketPrice = ticketPrice;
    }

    public String getUserId() {
        return userId;
    }

    public String getConcertId() {
        return concertId;
    }

    public String getSeatId() {
        return seatId;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }
}
