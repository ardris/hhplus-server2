package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.model.QueueToken;
import java.time.LocalDateTime;

/**
 * 대기열 토큰 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 * Domain Model과 분리하여 기술적 의존성을 격리했습니다.
 */
@Entity
@Table(name = "queue_token_info")
public class QueueTokenEntity {
    
    @Id
    @Column(name = "token_id")
    private String tokenId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "queue_position", nullable = false)
    private int queuePosition;
    
    @Column(name = "total_waiting_users", nullable = false)
    private int totalWaitingUsers;
    
    @Column(name = "estimated_wait_time")
    private Integer estimatedWaitTime;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Domain Model로 변환
     */
    public QueueToken toDomain() {
        return new QueueToken(
            this.tokenId,
            this.userId,
            this.queuePosition
        );
    }
    
    /**
     * Domain Model로부터 생성
     */
    public static QueueTokenEntity fromDomain(QueueToken queueToken) {
        QueueTokenEntity entity = new QueueTokenEntity();
        entity.tokenId = queueToken.getTokenId();
        entity.userId = queueToken.getUserId();
        entity.queuePosition = queueToken.getQueuePosition();
        entity.totalWaitingUsers = 0; // TODO: Domain Model에 totalWaitingUsers 필드 추가 필요
        entity.estimatedWaitTime = null; // TODO: Domain Model에 estimatedWaitTime 필드 추가 필요
        entity.isActive = queueToken.isActive();
        entity.issuedAt = queueToken.getIssuedAt();
        entity.expiresAt = queueToken.getExpiresAt();
        entity.activatedAt = queueToken.getActivatedAt();
        entity.createdAt = LocalDateTime.now(); // TODO: Domain Model에 createdAt 필드 추가 필요
        entity.updatedAt = null; // TODO: Domain Model에 updatedAt 필드 추가 필요
        return entity;
    }

    // Getters and Setters
    public String getTokenId() {
        return tokenId;
    }
    
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }
    
    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
