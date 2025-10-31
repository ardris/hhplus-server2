package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.domain.port.UserPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 예약 생성 UseCase
 */
@Service
public class MakeReservationUseCase {
    
    private final ReservationRepositoryPort reservationRepository;
    private final SeatPort seatPort;
    private final UserPort userPort;

    public MakeReservationUseCase(ReservationRepositoryPort reservationRepository, 
                                SeatPort seatPort, 
                                UserPort userPort) {
        this.reservationRepository = reservationRepository;
        this.seatPort = seatPort;
        this.userPort = userPort;
    }

    public Reservation execute(MakeReservationCommand command) {
        // 1. 사용자 존재 확인
        if (userPort.findById(command.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 2. 좌석 가용성 확인
        if (!seatPort.isSeatAvailable(command.getSeatId())) {
            throw new IllegalStateException("좌석을 예약할 수 없습니다: " + command.getSeatId());
        }

        // 3. 예약 생성
        Reservation reservation = Reservation.create(
                command.getUserId(),
                command.getConcertId(),
                command.getSeatId(),
                "A", // seatGrade 기본값
                command.getTicketPrice()
        );

        // 4. 좌석 임시 배정
        seatPort.holdSeat(command.getSeatId(), reservation.getReservationId());

        // 5. 예약 저장
        return reservationRepository.save(reservation);
    }

    /**
     * 예약 생성 명령 클래스
     */
    public static class MakeReservationCommand {
        private String userId;
        private String seatId;
        private String concertId;
        private BigDecimal ticketPrice;

        public MakeReservationCommand() {}

        public MakeReservationCommand(String userId, String seatId, String concertId, BigDecimal ticketPrice) {
            this.userId = userId;
            this.seatId = seatId;
            this.concertId = concertId;
            this.ticketPrice = ticketPrice;
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getSeatId() {
            return seatId;
        }
        
        public void setSeatId(String seatId) {
            this.seatId = seatId;
        }

        public String getConcertId() {
            return concertId;
        }
        
        public void setConcertId(String concertId) {
            this.concertId = concertId;
        }

        public BigDecimal getTicketPrice() {
            return ticketPrice;
        }
        
        public void setTicketPrice(BigDecimal ticketPrice) {
            this.ticketPrice = ticketPrice;
        }
    }
}