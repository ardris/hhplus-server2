package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 * Domain Model과 분리하여 기술적 의존성을 격리했습니다.
 */
@Entity
@Table(name = "user_master_info")
public class UserEntity {
    
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Domain Model로 변환
     */
    public User toDomain() {
        return new User(
            this.userId,
            this.username,
            this.email,
            this.balance,
            this.createdAt,
            this.updatedAt
        );
    }
    
    /**
     * Domain Model로부터 생성
     */
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.userId = user.getUserId();
        entity.username = user.getName();
        entity.email = user.getEmail();
        entity.phoneNumber = null; // TODO: Domain Model에 phoneNumber 필드 추가 필요
        entity.balance = user.getBalance();
        entity.isActive = true; // TODO: Domain Model에 isActive 필드 추가 필요
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();
        return entity;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
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
