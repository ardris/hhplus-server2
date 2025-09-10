package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.model.ConcertSeatResponse;
import kr.hhplus.be.server.model.Seat;
import kr.hhplus.be.server.model.SeatResponse;
import kr.hhplus.be.server.repository.ConcertRepository;
import kr.hhplus.be.server.repository.SeatRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 콘서트 관리 서비스 구현체
 */
public class ConcertServiceImpl implements ConcertService {
    
    private static final int TOTAL_SEAT_COUNT = 50;//좌석 50개 설정

    //가격 한번 해보자
    private static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("100000");
    
    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;
    
    public ConcertServiceImpl(ConcertRepository concertRepository, SeatRepository seatRepository) {
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
    }
    
    @Override
    // 예약 가능한 콘서트 목록 조회
    public List<Concert> getAvailableConcerts() {
        return concertRepository.findAll();
    }
    
    @Override
    // 특정 날짜 범위의 예약 가능한 콘서트 목록 조회
    public List<Concert> getAvailableConcertsByDateRange(String startDate, String endDate) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("시작 날짜는 필수입니다.");
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            throw new IllegalArgumentException("종료 날짜는 필수입니다.");
        }
        
        // 날짜 형식 검증 (간단한 검증)
        if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}") || !endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("날짜 형식은 yyyy-MM-dd여야 합니다.");
        }
        
        // 시작 날짜가 종료 날짜보다 늦으면 안됨
        if (startDate.compareTo(endDate) > 0) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
        
        List<Concert> allConcerts = concertRepository.findAll();
        List<Concert> filteredConcerts = new ArrayList<>();
        
        for (Concert concert : allConcerts) {
            String concertDate = concert.getDate();
            // 날짜 범위 내에 있는 콘서트만 필터링
            if (concertDate.compareTo(startDate) >= 0 && concertDate.compareTo(endDate) <= 0) {
                filteredConcerts.add(concert);
            }
        }
        
        return filteredConcerts;
    }
    
    @Override
    // 예약 가능한 콘서트 날짜 목록 조회
    public List<String> getAvailableConcertDates() {
        return concertRepository.getAvailableConcertDates();
    }
    
    @Override
    // 특정 날짜의 예약 가능한 좌석 목록 조회
    public List<String> getAvailableSeatsByDate(String concertDate) {
        if (concertDate == null || concertDate.trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 날짜는 필수입니다.");
        }
        
        // 만료된 임시 배정 정리
        cleanupExpiredHolds(concertDate);
        
        return seatRepository.getAvailableSeatIdsByConcertDate(concertDate);
    }
    
    @Override
    // 특정 콘서트의 좌석 상세 정보 조회
    public ConcertSeatResponse getConcertSeatDetails(String concertDate, String concertTitle) {
        if (concertDate == null || concertDate.trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 날짜는 필수입니다.");
        }
        if (concertTitle == null || concertTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 제목은 필수입니다.");
        }
        
        // 만료된 임시 배정 정리
        cleanupExpiredHolds(concertDate);
        
        // 콘서트 정보 조회
        Concert concert = concertRepository.findByDateAndTitle(concertDate, concertTitle);
        if (concert == null) {
            throw new IllegalArgumentException("해당 날짜와 제목의 콘서트를 찾을 수 없습니다.");
        }
        
        // 좌석 정보 조회
        List<Seat> seats = seatRepository.findByConcertDate(concertDate);
        List<SeatResponse> seatResponses = new ArrayList<>();
        int availableCount = 0;
        
        for (Seat seat : seats) {
            String status = seat.getStatus().toString();
            if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
                availableCount++;
            }
            
            seatResponses.add(new SeatResponse(
                seat.getSeatId(),
                Integer.parseInt(seat.getSeatId()),
                status
            ));
        }
        
        // 콘서트 정보 생성
        ConcertSeatResponse.ConcertInfo concertInfo = new ConcertSeatResponse.ConcertInfo(
            concert.getDate(),
            concert.getTitle(),
            concert.getTicketPrice()
        );
        
        return new ConcertSeatResponse(
            concertInfo,
            seatResponses,
            seats.size(),
            availableCount
        );
    }
    
    @Override
    // 콘서트 및 좌석 초기화
    public void initializeConcerts() {
        // 향후 7일간의 콘서트 생성
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate concertDate = LocalDate.now().plusDays(dayOffset);
            String concertId = "CONCERT_" + concertDate.toString();
            
            Concert concert = new Concert(
                concertId,
                "콘서트 " + (dayOffset + 1) + "일차",
                concertDate,
                TOTAL_SEAT_COUNT,
                DEFAULT_TICKET_PRICE
            );
            
            concertRepository.save(concert);
            
            // 좌석 생성
            for (int seatNumber = 1; seatNumber <= TOTAL_SEAT_COUNT; seatNumber++) {
                Seat seat = new Seat(String.valueOf(seatNumber), concertDate.toString());
                seatRepository.save(seat);
            }
        }
    }
    
    /**
     * 만료된 임시 배정을 정리합니다.
     */
    // 만료된 임시 배정 정리
    private void cleanupExpiredHolds(String concertDate) {
        List<Seat> seats = seatRepository.findByConcertDate(concertDate);
        
        for (Seat seat : seats) {
            if (seat.isHoldExpired()) {
                seat.releaseHold();
                seatRepository.update(seat);
            }
        }
    }
}
