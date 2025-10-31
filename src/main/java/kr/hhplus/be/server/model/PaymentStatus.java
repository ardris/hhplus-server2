package kr.hhplus.be.server.model;

/**
 * 결제 상태를 나타내는 열거형
 */
public enum PaymentStatus {
    PENDING,    // 대기 중
    COMPLETED,   // 완료
    FAILED,      // 실패
    CANCELLED,   // 취소
    REFUNDED     // 환불
}
