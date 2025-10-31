package kr.hhplus.be.server.model;

/**
 * 결제 방법을 나타내는 열거형
 */
public enum PaymentMethod {
    BALANCE,        // 잔액
    CARD,           // 카드
    BANK_TRANSFER   // 계좌이체
}
