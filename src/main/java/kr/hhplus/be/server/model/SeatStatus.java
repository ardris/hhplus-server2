package kr.hhplus.be.server.model;

/**
 * 좌석 상태를 나타내는 열거형
 */
public enum SeatStatus {
    AVAILABLE,  // 예약 가능
    HOLD,       // 임시 배정
    SOLD,       // 판매됨
    EXPIRED     // 만료
}
