package kr.hhplus.be.server.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.domain.port.UserPort;

/**
 * 다중 좌석 예약 Use Case (클린 아키텍처 - 애플리케이션 레이어)
 */
@Service
public class MakeMultiReservationUseCase {
    private final ReservationRepositoryPort reservationRepository;
    private final SeatPort seatPort;
    private final UserPort userPort;

    public MakeMultiReservationUseCase(ReservationRepositoryPort reservationRepository,
            SeatPort seatPort,
            UserPort userPort) {
        this.reservationRepository = reservationRepository;
        this.seatPort = seatPort;
        this.userPort = userPort;
    }

    public MultiReservationResult execute(MakeMultiReservationCommand command) {
        // 1. 사용자 존재 확인
        if (userPort.findById(command.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 2. 모든 좌석 가용성 확인
        for (String seatId : command.getSeatIds()) {
            if (!seatPort.isSeatAvailable(seatId)) {
                throw new IllegalStateException("좌석을 예약할 수 없습니다: " + seatId);
            }
        }

        // 3. 예약 그룹 ID 생성
        String reservationGroupId = "GROUP-" + UUID.randomUUID().toString().substring(0, 8);
        List<Reservation> reservations = new ArrayList<>();

        try {
            // 4. 모든 좌석에 대해 예약 생성
            for (String seatId : command.getSeatIds()) {
                Reservation reservation = Reservation.create(
                        command.getUserId(),
                        command.getPerformanceId(),
                        seatId,
                        "A", // seatGrade 기본값
                        command.getTicketPrice());

                // 5. 좌석 임시 배정
                seatPort.holdSeat(seatId, reservation.getReservationId());
                reservations.add(reservation);
            }

            // 6. 모든 예약 저장
            List<Reservation> savedReservations = new ArrayList<>();
            for (Reservation reservation : reservations) {
                savedReservations.add(reservationRepository.save(reservation));
            }

            return MultiReservationResult.success(reservationGroupId, savedReservations);

        } catch (Exception e) {
            // 7. 실패 시 모든 좌석 해제
            for (String seatId : command.getSeatIds()) {
                seatPort.releaseSeat(seatId);
            }
            return MultiReservationResult.failure(e.getMessage());
        }
    }

    /**
     * 다중 예약 명령 객체
     */
    public static class MakeMultiReservationCommand {
        private String userId;
        private String performanceId;
        private List<String> seatIds;
        private BigDecimal ticketPrice;

        public MakeMultiReservationCommand(String userId, String performanceId, List<String> seatIds,
                BigDecimal ticketPrice) {
            this.userId = userId;
            this.performanceId = performanceId;
            this.seatIds = seatIds;
            this.ticketPrice = ticketPrice;
        }

        public String getUserId() {
            return userId;
        }

        public String getPerformanceId() {
            return performanceId;
        }

        public List<String> getSeatIds() {
            return seatIds;
        }

        public BigDecimal getTicketPrice() {
            return ticketPrice;
        }
    }

    /**
     * 다중 예약 결과 객체
     */
    public static class MultiReservationResult {
        private boolean success;
        private String message;
        private String reservationGroupId;
        private List<Reservation> reservations;

        private MultiReservationResult(boolean success, String message, String reservationGroupId,
                List<Reservation> reservations) {
            this.success = success;
            this.message = message;
            this.reservationGroupId = reservationGroupId;
            this.reservations = reservations;
        }

        public static MultiReservationResult success(String reservationGroupId, List<Reservation> reservations) {
            return new MultiReservationResult(true, "예약이 완료되었습니다.", reservationGroupId, reservations);
        }

        public static MultiReservationResult failure(String message) {
            return new MultiReservationResult(false, message, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getReservationGroupId() {
            return reservationGroupId;
        }

        public List<Reservation> getReservations() {
            return reservations;
        }
    }
}
