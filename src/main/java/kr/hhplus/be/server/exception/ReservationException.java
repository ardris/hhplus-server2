package kr.hhplus.be.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 예약 관련 예외
 */
public class ReservationException extends ConcertReservationException {
    
    // 예약 예외 생성
    public ReservationException(String message) {
        super(HttpStatus.BAD_REQUEST, "RESERVATION_ERROR", message);
    }
    
    // 예약 예외 생성 (원인 포함)
    public ReservationException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "RESERVATION_ERROR", message, cause);
    }
    
    public static class SeatAlreadySoldException extends ConcertReservationException {
        // 좌석 이미 판매 완료 예외 (410 Gone - 리소스가 영구적으로 제거됨)
        public SeatAlreadySoldException() {
            super(HttpStatus.GONE, "SEAT_ALREADY_SOLD", "이미 판매 완료된 좌석입니다.");
        }
    }
    
    public static class SeatAlreadyHeldException extends ConcertReservationException {
        // 좌석 이미 임시 배정 예외 (423 Locked - 리소스가 잠겨있음)
        public SeatAlreadyHeldException() {
            super(HttpStatus.LOCKED, "SEAT_ALREADY_HELD", "이미 다른 사용자에게 임시배정된 좌석입니다.");
        }
    }
    
    public static class SeatNotAvailableException extends ReservationException {
        // 좌석 예약 불가 예외
        public SeatNotAvailableException() {
            super("해당 좌석을 예약할 수 없습니다.");
        }
    }
    
    public static class HoldExpiredException extends ReservationException {
        // 임시 배정 만료 예외
        public HoldExpiredException(String message) {
            super(message);
        }
    }
    
    public static class ReservationStateException extends ReservationException {
        // 예약 상태 오류 예외
        public ReservationStateException(String message) {
            super(message);
        }
    }
    
    @ResponseStatus(HttpStatus.GONE)
    public static class SeatNotFoundException extends ConcertReservationException {
        // 좌석을 찾을 수 없음 예외 (410 Gone - 리소스가 영구적으로 제거됨)
        public SeatNotFoundException() {
            super(HttpStatus.GONE, "SEAT_NOT_FOUND", "존재하지 않는 좌석입니다.");
        }
    }
}
