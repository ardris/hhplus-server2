package kr.hhplus.be.server.domain.port;

/**
 * 좌석 관련 포트 (클린 아키텍처 - 도메인 레이어)
 *
 * 핵심 개념:
 * 1. 도메인이 외부에 요구하는 좌석 관련 기능을 정의
 * 2. 인터페이스로 정의하여 구현체에 의존하지 않음
 * 3. 좌석 상태 관리 기능을 캡슐화
 */
public interface SeatPort {
    /**
     * 좌석 가용성 확인
     * @param seatId 좌석 ID
     * @return 좌석 사용 가능 여부
     */
    boolean isSeatAvailable(String seatId);

    /**
     * 좌석 임시 배정
     * @param seatId 좌석 ID
     * @param reservationId 예약 ID
     */
    void holdSeat(String seatId, String reservationId);

    /**
     * 좌석 판매 완료
     * @param seatId 좌석 ID
     */
    void sellSeat(String seatId);

    /**
     * 좌석 해제
     * @param seatId 좌석 ID
     */
    void releaseSeat(String seatId);
}
