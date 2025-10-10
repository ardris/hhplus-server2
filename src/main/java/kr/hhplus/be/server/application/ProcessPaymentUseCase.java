package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.PaymentRepositoryPort;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.domain.port.UserPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 결제 처리 Use Case
 * 
 * 클린아키텍처의 애플리케이션 레이어에서 복잡한 결제 비즈니스 로직을 처리해.
 * 여러 포트를 조합해서 트랜잭션과 예외 처리까지 포함한 완전한 결제 시나리오를 구현했어.
 */
@Service
public class ProcessPaymentUseCase {
    private final PaymentRepositoryPort paymentRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final SeatPort seatPort;
    private final UserPort userPort;

    /**
     * 의존성 주입
     * - 4개의 포트에 의존하여 복잡한 비즈니스 로직 구현
     * - 각 포트는 독립적으로 Mock 처리 가능
     */
    public ProcessPaymentUseCase(PaymentRepositoryPort paymentRepository,
            ReservationRepositoryPort reservationRepository,
            SeatPort seatPort,
            UserPort userPort) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.seatPort = seatPort;
        this.userPort = userPort;
    }

    /**
     * 결제 처리
     * 시나리오:
     * 1. 예약 조회 및 상태 확인
     * 2. 사용자 잔액 확인
     * 3. 결제 생성
     * 4. 잔액 차감
     * 5. 결제 완료
     * 6. 예약 완료
     * 7. 좌석 판매 완료
     * 8. 저장
     */
    public PaymentResult execute(ProcessPaymentCommand command) {
        // 1. 예약 조회 및 상태 확인
        Optional<Reservation> reservationOpt = reservationRepository.findById(command.getReservationId());
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }

        Reservation reservation = reservationOpt.get();
        if (!reservation.canBePaid()) {
            throw new IllegalStateException("결제할 수 없는 예약입니다.");
        }

        // 2. 사용자 잔액 확인
        if (!userPort.hasEnoughBalance(reservation.getUserId(), reservation.getTicketPrice())) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        // 3. 결제 생성 (도메인 엔티티의 팩토리 메서드 사용)
        Payment payment = Payment.create(
                reservation.getUserId(),
                reservation.getReservationId(),
                reservation.getTicketPrice());

        try {
            // 4. 잔액 차감 (포트를 통해 외부 시스템 호출)
            userPort.deductBalance(reservation.getUserId(), reservation.getTicketPrice());

            // 5. 결제 완료 (도메인 엔티티의 비즈니스 로직 사용)
            payment.complete();

            // 6. 예약 완료 (도메인 엔티티의 비즈니스 로직 사용)
            reservation.completePayment(payment.getPaymentId());

            // 7. 좌석 판매 완료 (포트를 통해 외부 시스템 호출)
            seatPort.sellSeat(reservation.getSeatId());

            // 8. 저장 (포트를 통해 외부 시스템 호출)
            paymentRepository.save(payment);
            reservationRepository.save(reservation);

            return PaymentResult.success(payment.getPaymentId(), "결제가 완료되었습니다.");

        } catch (Exception e) {
            // 결제 실패 처리 (도메인 엔티티의 비즈니스 로직 사용)
            payment.fail(e.getMessage());
            paymentRepository.save(payment);
            return PaymentResult.failure(e.getMessage());
        }
    }
}

/**
 * 결제 처리 명령 객체
 */
class ProcessPaymentCommand {
    private String reservationId;

    public ProcessPaymentCommand(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationId() {
        return reservationId;
    }
}

/**
 * 결제 결과 객체
 * 
 * 핵심 개념:
 * 1. Use Case의 결과를 캡슐화
 * 2. 성공/실패 상태와 메시지 포함
 * 3. 도메인 엔티티가 아닌 단순한 결과 객체
 */
class PaymentResult {
    private boolean success;
    private String message;
    private String paymentId;

    private PaymentResult(boolean success, String message, String paymentId) {
        this.success = success;
        this.message = message;
        this.paymentId = paymentId;
    }

    public static PaymentResult success(String paymentId, String message) {
        return new PaymentResult(true, message, paymentId);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, message, null);
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
