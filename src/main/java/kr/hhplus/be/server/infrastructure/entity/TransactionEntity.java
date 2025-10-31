package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 내역 JPA 엔티티
 */
@Entity
@Table(name = "transaction_history")
public class TransactionEntity {
    
    @Id
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Domain Model로 변환
     */
    public kr.hhplus.be.server.model.Transaction toDomain() {
        kr.hhplus.be.server.model.Transaction transaction = new kr.hhplus.be.server.model.Transaction(
            this.userId,
            kr.hhplus.be.server.model.Transaction.TransactionType.valueOf(this.transactionType),
            this.amount,
            this.balanceAfter,
            this.description
        );
        // 생성자에서 자동 설정되지 않는 필드들을 수동으로 설정
        transaction.setTransactionId(this.transactionId);
        transaction.setCreatedAt(this.createdAt);
        return transaction;
    }
}