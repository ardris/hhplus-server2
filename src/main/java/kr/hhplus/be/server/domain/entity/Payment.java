package kr.hhplus.be.server.domain.entity;

import kr.hhplus.be.server.model.PaymentStatus;
import kr.hhplus.be.server.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 도메인 엔티티
 * 
 * 클린아키텍처의 도메인 레이어에서 결제 관련 비즈니스 규칙을 담고 있어.
 * 외부 의존성 없이 순수한 Java 객체로 결제 상태 관리를 구현했어.
 */
public class Payment {
    private String paymentId;
    private String userId;
    private String reservationId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;

    // 생성자
    public Payment(String paymentId, String userId, String reservationId,
            BigDecimal amount, PaymentMethod paymentMethod) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // 간단한 생성자 (기존 코드 호환성)
    public Payment(String paymentId, String userId, String reservationId, BigDecimal amount) {
        this(paymentId, userId, reservationId, amount, PaymentMethod.BALANCE);
    }

    /**
     * 비즈니스 로직 1: 결제 완료
     * - 도메인 규칙: PENDING 상태에서만 완료 가능
     */
    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제할 수 없는 상태입니다.");
        }
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 비즈니스 로직 2: 결제 실패
     * - 도메인 규칙: PENDING 상태에서만 실패 처리 가능
     */
    public void fail(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 상태를 변경할 수 없습니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    /**
     * 비즈니스 로직 3: 결제 취소
     * - 도메인 규칙: 완료된 결제는 취소 불가
     */
    public void cancel() {
        if (this.status == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제는 취소할 수 없습니다.");
        }
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * 비즈니스 로직 4: 결제 완료 여부
     */
    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    /**
     * 비즈니스 로직 5: 결제 실패 여부
     */
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    /**
     * 비즈니스 로직 6: 결제 상태 업데이트
     */
    public void updateStatus(PaymentStatus status) {
        this.status = status;
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * 비즈니스 로직 7: 결제 상태 업데이트 (외부에서 호출)
     */
    public void updateStatus(PaymentStatus status, String reason) {
        this.status = status;
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) {
            this.completedAt = LocalDateTime.now();
        }
        if (status == PaymentStatus.FAILED) {
            this.failureReason = reason;
        }
    }

    /**
     * 정적 팩토리 메서드
     * - 도메인 규칙: 결제 ID 자동 생성
     */
    public static Payment create(String userId, String reservationId, BigDecimal amount) {
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        return new Payment(paymentId, userId, reservationId, amount, PaymentMethod.BALANCE);
    }

    // Getters
    public String getPaymentId() {
        return paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
