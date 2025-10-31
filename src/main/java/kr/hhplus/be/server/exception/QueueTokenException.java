package kr.hhplus.be.server.exception;

import org.springframework.http.HttpStatus;

/**
 * 대기열 토큰 관련 예외
 */
public class QueueTokenException extends ConcertReservationException {
    
    // 토큰 예외 생성
    public QueueTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "QUEUE_TOKEN_ERROR", message);
    }
    
    // 토큰 예외 생성 (원인 포함)
    public QueueTokenException(String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, "QUEUE_TOKEN_ERROR", message, cause);
    }
    
    public static class InvalidTokenException extends ConcertReservationException {
        // 유효하지 않은 토큰 예외 (401 Unauthorized - 인증 실패)
        public InvalidTokenException() {
            super(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }
    }
    
    public static class TokenExpiredException extends ConcertReservationException {
        // 토큰 만료 예외 (401 Unauthorized - 토큰 만료)
        public TokenExpiredException() {
            super(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다. 다시 발급받아 주세요.");
        }
    }
    
    public static class LatestTokenMismatchException extends QueueTokenException {
        // 최신 토큰 불일치 예외
        public LatestTokenMismatchException() {
            super("다른 세션에서 발급된 최신 토큰이 존재합니다.");
        }
    }
    
    public static class NotActiveInQueueException extends ConcertReservationException {
        // 대기열 비활성 상태 예외 (425 Too Early - 요청이 너무 이르게 전송됨)
        public NotActiveInQueueException(int waitingCount) {
            super(HttpStatus.TOO_EARLY, "NOT_ACTIVE_IN_QUEUE", 
                  "아직 활성화되지 않았습니다. 잠시만 기다려 주세요. (대기 인원 : " + waitingCount + ")");
        }
    }
}
