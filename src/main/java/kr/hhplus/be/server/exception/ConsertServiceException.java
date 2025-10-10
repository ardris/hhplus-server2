package kr.hhplus.be.server.exception;

import org.springframework.http.HttpStatus;

/**
 * 1주차 리뷰 -> 예외 클래스 생성으로 500 코드로 처리되는 항목을 부ㅠㄴ류
 * 
 * 큐토큰익셉션 => 401 코드로 처리되는 항목
 * 리젠션익셉션 => 400 코드로 처리되는 항목
 * 페이먼트익셉션 => 400 코드로 처리되는 항목
 * 
 * 
 * 콘서트 예약 서비스의 기본 예외 클래스
 */
public abstract class ConsertServiceException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    // 예외 객체 생성
    protected ConsertServiceException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    // 예외 객체 생성 (원인 )
    protected ConsertServiceException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    // HTTP 상태 코드 조회
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    // 에러 코드 조회
    public String getErrorCode() {
        return errorCode;
    }
}
