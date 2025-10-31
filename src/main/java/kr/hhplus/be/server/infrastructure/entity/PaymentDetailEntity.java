package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 상세 정보 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "payment_detail_info")
@Getter
@Setter
@NoArgsConstructor
public class PaymentDetailEntity {
    
    @Id
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "payment_gateway")
    private String paymentGateway;
    
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
