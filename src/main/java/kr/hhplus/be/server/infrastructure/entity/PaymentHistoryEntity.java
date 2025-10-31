package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 결제 히스토리 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "payment_history")
@Getter
@Setter
@NoArgsConstructor
public class PaymentHistoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    @Column(name = "payment_id", nullable = false)
    private String paymentId;
    
    @Column(name = "status_from")
    private String statusFrom;
    
    @Column(name = "status_to", nullable = false)
    private String statusTo;
    
    @Column(name = "change_reason")
    private String changeReason;
    
    @Column(name = "performed_by")
    private String performedBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
