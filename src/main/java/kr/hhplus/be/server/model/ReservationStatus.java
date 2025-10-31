package kr.hhplus.be.server.model;

/**
 * 예약 상태를 나타내는 열거형
 */
public enum ReservationStatus {
    AVAILABLE,   // 예약 가능
    HOLD,        // 임시 배정
    PAID,        // 결제 완료
    EXPIRED,     // 만료
    CANCELLED    // 취소
}
