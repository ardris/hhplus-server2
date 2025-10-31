package kr.hhplus.be.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자 정보를 관리하는 도메인 모델
 * 
 * 콘서트 예약 서비스에서 사용자의 기본 정보와 잔액을 관리하는 클래스입니다.
 * 사용자가 좌석을 예약하고 결제할 때 필요한 잔액 정보를 담당합니다.
 */
public class User {
    
    private String userId;
    private String name;
    private String email;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public User(String userId) {
        this.userId = userId;
        this.name = "사용자" + userId;
        this.email = null;
        this.balance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public User(String userId, String name, String email, BigDecimal balance, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    

    /**
     * 사용자 잔액을 충전합니다.
     * 
     * @param amount 충전할 금액
     * @throws IllegalArgumentException 충전 금액이 0 이하일 때
     * 
     * 이유: 사용자가 카드나 계좌로 잔액을 충전할 때 사용됩니다.
     * 음수나 0원 충전을 방지하여 비즈니스 로직의 무결성을 보장합니다.
     * 충전 후 updatedAt 시간을 갱신하여 마지막 수정 시점을 추적합니다.
     */
    // 잔액 충전
    public void chargeBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 사용자 잔액에서 금액을 차감합니다.
     * 
     * @param amount 차감할 금액
     * @throws IllegalArgumentException 차감 금액이 0 이하이거나 잔액이 부족할 때
     * 
     * 이유: 사용자가 좌석 예약 결제를 할 때 잔액에서 차감하는 용도입니다.
     * 잔액 부족으로 인한 결제 실패를 사전에 방지하고, 
     * 음수 차감을 막아 데이터 무결성을 보장합니다.
     */
    // 잔액 차감
    public void deductBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 사용자가 충분한 잔액을 가지고 있는지 확인합니다.
     * 
     * @param amount 확인할 금액
     * @return true: 잔액 충분, false: 잔액 부족
     * 
     * 이유: 결제 전에 잔액을 미리 확인하여 결제 실패를 방지합니다.
     * UI에서 결제 버튼 활성화/비활성화를 결정하거나,
     * 서비스 레이어에서 사전 검증할 때 사용됩니다.
     */
    // 잔액 충분 여부 확인
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
