package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.model.ConcertSeatResponse;
import kr.hhplus.be.server.model.Seat;
import kr.hhplus.be.server.model.SeatResponse;
import kr.hhplus.be.server.infrastructure.repository.JpaConcertRepository;
import kr.hhplus.be.server.infrastructure.repository.JpaSeatRepository;
import kr.hhplus.be.server.infrastructure.entity.ConcertEntity;
import kr.hhplus.be.server.infrastructure.entity.SeatEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 콘서트 관리 서비스 구현체
 */
@Service
public class ConcertServiceImpl implements ConcertService {
    
    private static final int TOTAL_SEAT_COUNT = 50;
    private static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("100000");
    
    private final JpaConcertRepository jpaConcertRepository;
    private final JpaSeatRepository jpaSeatRepository;
    
    @Autowired
    public ConcertServiceImpl(JpaConcertRepository jpaConcertRepository, JpaSeatRepository jpaSeatRepository) {
        this.jpaConcertRepository = jpaConcertRepository;
        this.jpaSeatRepository = jpaSeatRepository;
    }
    
    @Override
    public List<Concert> getAvailableConcerts() {
        return jpaConcertRepository.findActiveConcerts().stream()
                .map(ConcertEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
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
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        return jpaConcertRepository.findConcertsByDate(start).stream()
                .filter(concert -> {
                    LocalDate concertStart = concert.getConcertPeriodStart();
                    LocalDate concertEnd = concert.getConcertPeriodEnd();
                    return !concertStart.isAfter(end) && !concertEnd.isBefore(start);
                })
                .map(ConcertEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    // 예약 가능한 콘서트 날짜 목록 조회
    public List<String> getAvailableConcertDates() {
        return jpaConcertRepository.findActiveConcerts().stream()
                .map(ConcertEntity::getConcertPeriodStart)
                .map(LocalDate::toString)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Override
    // 특정 날짜의 예약 가능한 좌석 목록 조회
    public List<String> getAvailableSeatsByDate(String concertDate) {
        if (concertDate == null || concertDate.trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 날짜는 필수입니다.");
        }
        
        // 만료된 임시 배정 정리
        cleanupExpiredHolds(concertDate);
        
        // TODO: DB 스키마 변경으로 인해 좌석 조회 로직 재구현 필요
        // SeatEntity는 이제 venue_id 기반이고, 예약 상태는 seat_reservation_status 테이블에서 관리
        return new ArrayList<>(); // 임시 반환
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
        ConcertEntity concertEntity = jpaConcertRepository.findByDateAndTitle(concertDate, concertTitle)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜와 제목의 콘서트를 찾을 수 없습니다."));
        Concert concert = concertEntity.toDomain();
        
        // TODO: DB 스키마 변경으로 인해 좌석 조회 로직 재구현 필요
        List<Seat> seats = new ArrayList<>(); // 임시 반환
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
            concert.getConcertPeriodStart().toString(),
            concert.getConcertName(),
            DEFAULT_TICKET_PRICE
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
                concertDate.plusDays(1)
            );
            
            ConcertEntity concertEntity = ConcertEntity.fromDomain(concert);
            jpaConcertRepository.save(concertEntity);
            
            // TODO: DB 스키마 변경으로 인해 좌석 생성 로직 재구현 필요
            // SeatEntity는 이제 venue_id 기반이며, 공연장별로 좌석이 관리됨
            // 좌석 생성은 공연장 등록 시 한 번만 수행하고, 
            // 공연별 좌석 상태는 seat_reservation_status 테이블에서 관리
        }
    }
    
    /**
     * 만료된 임시 배정을 정리합니다.
     * 
     * TODO: DB 스키마 변경으로 인해 재구현 필요
     * seat_reservation_status 테이블에서 만료된 HOLD 상태를 조회하고
     * AVAILABLE 상태로 변경하는 로직으로 수정
     */
    private void cleanupExpiredHolds(String concertDate) {
        // TODO: 스키마 변경 후 재구현
    }
}
