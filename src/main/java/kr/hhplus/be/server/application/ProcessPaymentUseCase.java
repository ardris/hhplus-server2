package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.model.PaymentMethod;
import kr.hhplus.be.server.model.PaymentStatus;
import kr.hhplus.be.server.domain.port.PaymentRepositoryPort;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.service.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 결제 처리 UseCase
 */
@Service
public class ProcessPaymentUseCase {
    
    private final PaymentRepositoryPort paymentRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final UserService userService;

    public ProcessPaymentUseCase(PaymentRepositoryPort paymentRepository, 
                                ReservationRepositoryPort reservationRepository, 
                                UserService userService) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.userService = userService;
    }

    public PaymentResult execute(ProcessPaymentCommand command) {
        // 예약 정보 조회
        var reservation = reservationRepository.findById(command.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        // 결제 처리
        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                reservation.getUserId(),
                reservation.getReservationId(),
                reservation.getTicketPrice(),
                PaymentMethod.BALANCE
        );

        // 사용자 잔액 차감
        userService.deductBalance(reservation.getUserId(), reservation.getTicketPrice());

        // 결제 상태 업데이트
        payment.updateStatus(PaymentStatus.COMPLETED);

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResult(savedPayment.getPaymentId(), PaymentStatus.COMPLETED, "결제가 완료되었습니다.");
    }

    /**
     * 결제 처리 명령 클래스
     */
    public static class ProcessPaymentCommand {
        private String reservationId;

        public ProcessPaymentCommand() {}

        public ProcessPaymentCommand(String reservationId) {
            this.reservationId = reservationId;
        }

        public String getReservationId() {
            return reservationId;
        }
        
        public void setReservationId(String reservationId) {
            this.reservationId = reservationId;
        }
    }

    /**
     * 결제 결과 클래스
     */
    public static class PaymentResult {
        private String paymentId;
        private PaymentStatus status;
        private String message;

        public PaymentResult() {}

        public PaymentResult(String paymentId, PaymentStatus status, String message) {
            this.paymentId = paymentId;
            this.status = status;
            this.message = message;
        }

        // Getters and Setters
        public String getPaymentId() {
            return paymentId;
        }
        
        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public PaymentStatus getStatus() {
            return status;
        }
        
        public void setStatus(PaymentStatus status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public boolean isSuccess() {
            return status == PaymentStatus.COMPLETED;
        }
        
        public Payment getPayment() {
            // 실제 구현에서는 Payment 객체를 반환해야 하지만, 
            // 현재는 간단히 null을 반환합니다.
            return null;
        }
    }
}