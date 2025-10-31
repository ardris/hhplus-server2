package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 * Domain Entity와 분리하여 기술적 의존성을 격리했습니다.
 */
@Entity
@Table(name = "payment_master_info")
public class PaymentEntity {
    
    @Id
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "reservation_id", nullable = false)
    private String reservationId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Domain Entity로 변환
     */
    public Payment toDomain() {
        Payment payment = new Payment(
            this.paymentId,
            this.userId,
            this.reservationId,
            this.amount,
            kr.hhplus.be.server.model.PaymentMethod.valueOf(this.paymentMethod)
        );
        
        // 추가 필드 설정
        payment.updateStatus(this.status, null);
        if (this.completedAt != null) {
            // completedAt은 updateStatus에서 자동 설정됨
        }
        
        return payment;
    }
    
    /**
     * Domain Entity로부터 생성
     */
    public static PaymentEntity fromDomain(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.paymentId = payment.getPaymentId();
        entity.reservationId = payment.getReservationId();
        entity.userId = payment.getUserId();
        entity.amount = payment.getAmount();
        entity.status = payment.getStatus();
        entity.paymentMethod = "BALANCE"; // TODO: Domain Entity에 paymentMethod 필드 추가 필요
        entity.createdAt = payment.getCreatedAt();
        entity.completedAt = payment.getCompletedAt();
        return entity;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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
}
