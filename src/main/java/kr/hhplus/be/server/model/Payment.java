package kr.hhplus.be.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 정보를 관리하는 도메인 모델
 * 

 */
public class Payment {
    
    private String paymentId;         // 결제 고유 식별자
    private String userId;            // 결제한 사용자 ID
    private String reservationId;     // 결제 대상 예약 ID
    private BigDecimal amount;          // 결제 금액 
    private PaymentStatus status;     // 결제 현재 상태 (PENDING, COMPLETED, FAILED, CANCELLED)
    private LocalDateTime createdAt;    // 결제 생성 시간
    private LocalDateTime completedAt; // 결제 완료/실패/취소 시간/*  */
    private String failureReason;      // 결제 실패 사유
    
    /**
     * 결제의 상태를 나타내는 열거형
     * 
     * PENDING: 결제 대기 상태 (결제 요청 후 처리 중)
     * COMPLETED: 결제 완료 상태 (성공적으로 처리됨)
     * FAILED: 결제 실패 상태 (잔액 부족, 시스템 오류 등)
     * CANCELLED: 결제 취소 상태 (사용자가 취소하거나 타임아웃)
     */
    public enum PaymentStatus {
        PENDING,    // 결제 대기
        COMPLETED,  // 결제 완료
        FAILED,     // 결제 실패
        CANCELLED   // 결제 취소
    }
    
    /**
     * 결제 객체를 생성합니다.
     * 
     * @param paymentId 결제 고유 식별자
     * @param userId 결제한 사용자 ID
     * @param reservationId 결제 대상 예약 ID
     * @param amount 결제 금액
     * 
     * 이유: 사용자가 예약에 대한 결제를 요청했을 때 호출됩니다.
     * 생성 시 status는 PENDING으로 설정되어 결제 처리 중 상태가 됩니다.
     * 이후 실제 결제 처리 결과에 따라 COMPLETED, FAILED, CANCELLED로 상태가 변경됩니다.
     */
    // 결제 객체 생성
    public Payment(String paymentId, String userId, String reservationId, BigDecimal amount) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;  // 기본적으로 결제 대기 상태
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 결제를 완료 처리합니다.
     * 
     * @throws IllegalStateException 결제가 PENDING 상태가 아닐 때
     * 
     * 이유: 사용자의 잔액에서 결제 금액이 성공적으로 차감되었을 때 호출됩니다.
     * PENDING 상태에서 COMPLETED 상태로 변경하여 결제가 성공적으로 완료되었음을 표시합니다.
     * completedAt 시간을 기록하여 결제 완료 시점을 추적할 수 있게 합니다.
     * 이 시점에서 예약도 PAID 상태로 변경되어 좌석 소유권이 확정됩니다.
     */
    // 결제 완료 처리
    public void completePayment() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 완료할 수 없는 상태입니다.");
        }
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 결제를 실패 처리합니다.
     * 
     * @param reason 결제 실패 사유
     * @throws IllegalStateException 결제가 PENDING 상태가 아닐 때
     * 
     * 이유: 잔액 부족, 시스템 오류, 네트워크 문제 등으로 결제가 실패했을 때 호출됩니다.
     * PENDING 상태에서 FAILED 상태로 변경하고, 실패 사유를 failureReason에 기록합니다.
     * 이 정보는 사용자에게 오류 메시지를 표시하거나, 재시도 로직을 결정하는 데 활용됩니다.
     * 실패한 결제는 나중에 정리 작업에서 처리될 수 있습니다.
     */
    // 결제 실패 처리
    public void failPayment(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 실패 처리할 수 없는 상태입니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 결제를 취소 처리합니다.
     * 
     * @throws IllegalStateException 결제가 PENDING 상태가 아닐 때
     * 
     * 이유: 사용자가 결제를 취소하거나, 타임아웃으로 인해 결제가 취소되었을 때 호출됩니다.
     * PENDING 상태에서 CANCELLED 상태로 변경하여 결제가 취소되었음을 표시합니다.
     * 취소된 결제는 더 이상 처리되지 않으며, 관련된 예약도 만료 처리될 수 있습니다.
     * completedAt 시간을 기록하여 취소 시점을 추적할 수 있게 합니다.
     */
    // 결제 취소 처리
    public void cancelPayment() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 취소할 수 없는 상태입니다.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 결제가 완료된 상태인지 확인합니다.
     * 
     * @return true: 결제 완료됨, false: 그렇지 않음
     * 
     * 이유: 결제의 최종 성공 상태를 확인할 때 사용됩니다.
     * COMPLETED 상태인 결제는 성공적으로 처리된 결제로, 예약이 확정된 상태입니다.
     * 사용자에게 결제 완료를 알리거나, 예약 상태를 업데이트할 때 활용됩니다.
     */
    // 결제 완료 여부 확인
    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }
    
    /**
     * 결제가 실패한 상태인지 확인합니다.
     * 
     * @return true: 결제 실패됨, false: 그렇지 않음
     * 
     * 이유: 결제 실패 여부를 확인하여 사용자에게 적절한 오류 메시지를 표시할 때 사용됩니다.
     * FAILED 상태인 결제는 실패한 결제로, failureReason을 통해 실패 사유를 확인할 수 있습니다.
     * UI에서 재시도 버튼을 표시하거나, 예약을 만료 처리할 때 활용됩니다.
     */
    // 결제 실패 여부 확인
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }
    
    /**
     * 결제가 대기 중인 상태인지 확인합니다.
     * 
     * @return true: 결제 대기 중, false: 그렇지 않음
     * 
     * 이유: 결제가 아직 처리 중인지 확인할 때 사용됩니다.
     * PENDING 상태인 결제는 아직 완료되지 않은 결제로, 상태 변경이 가능합니다.
     * 중복 결제를 방지하거나, 결제 상태를 업데이트할 수 있는지 확인할 때 활용됩니다.
     */
    // 결제 대기 상태 여부 확인
    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }
    
    

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
