package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Reservation;
import java.math.BigDecimal;
import java.util.List;

/**
 * 예약 관리 서비스 인터페이스
 */
public interface ReservationService {

    /**
     * 좌석을 임시 배정합니다.
     * 
     * @param tokenId     대기열 토큰 ID
     * @param concertDate 콘서트 날짜
     * @param seatId      좌석 ID
     * @param ticketPrice 티켓 가격
     * @return 생성된 예약 정보
     */
    Reservation holdSeat(String tokenId, String concertDate, String seatId, BigDecimal ticketPrice);

    /**
     * 좌석을 임시 배정합니다. (콘서트 제목 포함)
     * 
     * @param tokenId      대기열 토큰 ID
     * @param concertDate  콘서트 날짜
     * @param concertTitle 콘서트 제목
     * @param seatId       좌석 ID
     * @return 생성된 예약 정보
     */
    Reservation holdSeat(String tokenId, String concertDate, String concertTitle, String seatId);

    /**
     * 예약 정보를 조회합니다.
     * 
     * @param tokenId       대기열 토큰 ID
     * @param reservationId 예약 ID
     * @return 예약 정보
     */
    Reservation getReservation(String tokenId, String reservationId);

    /**
     * 사용자의 예약 목록을 조회합니다.
     * 
     * @param tokenId 대기열 토큰 ID
     * @return 예약 목록
     */
    List<Reservation> getUserReservations(String tokenId);

    /**
     * 만료된 임시 배정을 정리합니다.
     */
    void cleanupExpiredHolds();
}
