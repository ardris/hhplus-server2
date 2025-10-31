package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 거래 내역 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "user_history")
@Getter
@Setter
@NoArgsConstructor
public class UserHistoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "related_id")
    private String relatedId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
