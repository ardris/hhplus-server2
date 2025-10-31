package kr.hhplus.be.server.exception;

import org.springframework.http.HttpStatus;

/**
 * 콘서트 예약 서비스의 기본 예외 클래스
 */
public class ConcertReservationException extends RuntimeException {
    
    private HttpStatus status;
    private String errorCode;
    
    public ConcertReservationException(String message) {
        super(message);
    }
    
    public ConcertReservationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConcertReservationException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
    
    public ConcertReservationException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
