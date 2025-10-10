package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.PaymentRepositoryPort;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.domain.port.UserPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 다중 결제 처리 Use Case (클린 아키텍처 -적용 레이어)
 */
@Service
public class ProcessMultiPaymentUseCase {
    private final PaymentRepositoryPort paymentRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final SeatPort seatPort;
    private final UserPort userPort;

    public ProcessMultiPaymentUseCase(PaymentRepositoryPort paymentRepository,
            ReservationRepositoryPort reservationRepository,
            SeatPort seatPort,
            UserPort userPort) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.seatPort = seatPort;
        this.userPort = userPort;
    }

    public MultiPaymentResult execute(ProcessMultiPaymentCommand command) {
        List<Reservation> reservations = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. 모든 예약 조회 및 상태 확인
        for (String reservationId : command.getReservationIds()) {
            Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
            if (reservationOpt.isEmpty()) {
                throw new IllegalArgumentException("예약을 찾을 수 없습니다: " + reservationId);
            }

            Reservation reservation = reservationOpt.get();
            if (!reservation.canBePaid()) {
                throw new IllegalStateException("결제할 수 없는 예약입니다: " + reservationId);
            }

            reservations.add(reservation);
            totalAmount = totalAmount.add(reservation.getTicketPrice());
        }

        // 2. 사용자 잔액 확인
        if (!userPort.hasEnoughBalance(command.getUserId(), totalAmount)) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        // 3. 결제 생성 (도메인 엔티티)
        Payment payment = Payment.create(
                command.getUserId(),
                command.getReservationGroupId(),
                totalAmount);

        try {
            // 4. 잔액 차감
            userPort.deductBalance(command.getUserId(), totalAmount);

            // 5. 결제 완료 (도메인 엔티티 비즈니스 로직)
            payment.complete();

            // 6. 모든 예약 완료 처리
            for (Reservation reservation : reservations) {
                reservation.completePayment(payment.getPaymentId());
                seatPort.sellSeat(reservation.getSeatId());
                reservationRepository.save(reservation);
            }

            // 7. 결제 저장
            paymentRepository.save(payment);

            return MultiPaymentResult.success(payment.getPaymentId(), "결제가 완료되었습니다.");

        } catch (Exception e) {
            // 8. 결제 실패 처리
            payment.fail(e.getMessage());
            paymentRepository.save(payment);
            return MultiPaymentResult.failure(e.getMessage());
        }
    }

    /**
     * 다중 결제 명령 객체
     */
    public static class ProcessMultiPaymentCommand {
        private String userId;
        private String reservationGroupId;
        private List<String> reservationIds;

        public ProcessMultiPaymentCommand(String userId, String reservationGroupId, List<String> reservationIds) {
            this.userId = userId;
            this.reservationGroupId = reservationGroupId;
            this.reservationIds = reservationIds;
        }

        public String getUserId() {
            return userId;
        }

        public String getReservationGroupId() {
            return reservationGroupId;
        }

        public List<String> getReservationIds() {
            return reservationIds;
        }
    }

    /**
     * 다중 결제 결과 객체
     */
    public static class MultiPaymentResult {
        private boolean success;
        private String message;
        private String paymentId;

        private MultiPaymentResult(boolean success, String message, String paymentId) {
            this.success = success;
            this.message = message;
            this.paymentId = paymentId;
        }

        public static MultiPaymentResult success(String paymentId, String message) {
            return new MultiPaymentResult(true, message, paymentId);
        }

        public static MultiPaymentResult failure(String message) {
            return new MultiPaymentResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getPaymentId() {
            return paymentId;
        }
    }
}
