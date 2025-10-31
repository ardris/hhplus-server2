package kr.hhplus.be.server.exception;

import org.springframework.http.HttpStatus;
import java.math.BigDecimal;

/**
 * 결제 관련 예외 처리
 */
public class PaymentException extends ConcertReservationException {
    
    // 결제 예외 생성
    public PaymentException(String message) {
        super(HttpStatus.BAD_REQUEST, "PAYMENT_ERROR", message);
    }
    
    // 결제 예외 생성 (원인 포함)
    public PaymentException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "PAYMENT_ERROR", message, cause);
    }
    
    public static class InsufficientBalanceException extends PaymentException {
        private final BigDecimal currentBalance;
        private final BigDecimal requiredAmount;
        
        // 기본 생성자
        public InsufficientBalanceException() {
            super("잔액이 부족합니다.");
            this.currentBalance = BigDecimal.ZERO;
            this.requiredAmount = BigDecimal.ZERO;
        }
        
        // 잔액 부족 예외
        public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requiredAmount) {
            super(String.format("잔액이 부족합니다. 현재 잔액: %s, 필요 금액: %s", currentBalance, requiredAmount));
            this.currentBalance = currentBalance;
            this.requiredAmount = requiredAmount;
        }
        
        // 현재 잔액 조회
        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }
        
        // 필요 금액 조회
        public BigDecimal getRequiredAmount() {
            return requiredAmount;
        }
    }
}
