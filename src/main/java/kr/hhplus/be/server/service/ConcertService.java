package kr.hhplus.be.server.service;

import java.util.List;
import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.model.ConcertSeatResponse;

/**
 * 콘서트 관리 서비스 인터페이스 ( 날짜별 조회ㅏㄱ 필요할거같아 작성)
 */
public interface ConcertService {
    
    /**
     * 예약 가능한 콘서트 목록을 조회합니다.
     * @return 예약 가능한 콘서트 목록
     */
    List<Concert> getAvailableConcerts();
    
    /**
     * 특정 날짜 범위의 예약 가능한 콘서트 목록을 조회합니다.
     * @param startDate 시작 날짜 (yyyy-MM-dd)
     * @param endDate 종료 날짜 (yyyy-MM-dd)
     * @return 해당 기간의 예약 가능한 콘서트 목록
     */
    List<Concert> getAvailableConcertsByDateRange(String startDate, String endDate);
    
    /**
     * 예약 가능한 콘서트 날짜 목록을 조회합니다.
     * @return 예약 가능한 날짜 목록
     */
    List<String> getAvailableConcertDates();
    
    /**
     * 특정 날짜의 예약 가능한 좌석 목록을 조회합니다.
     * @param concertDate 콘서트 날짜 (yyyy-MM-dd)
     * @return 예약 가능한 좌석 ID 목록
     */
    List<String> getAvailableSeatsByDate(String concertDate);
    
    /**
     * 특정 콘서트의 좌석 상세 정보를 조회합니다.
     * @param concertDate 콘서트 날짜 (yyyy-MM-dd)
     * @param concertTitle 콘서트 제목
     * @return 콘서트 좌석 상세 정보
     */
    ConcertSeatResponse getConcertSeatDetails(String concertDate, String concertTitle);
    
    /**
     * 콘서트 정보를 초기화합니다.
     */
    void initializeConcerts();
}
