package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.model.ConcertSeatResponse;
import kr.hhplus.be.server.model.Seat;
import kr.hhplus.be.server.repository.ConcertRepository;
import kr.hhplus.be.server.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 콘서트 서비스 단위 테스트
 */
class ConcertServiceTest {

    private ConcertService concertService;
    private ConcertRepository concertRepository;
    private SeatRepository seatRepository;

    //신기한거 발견! 콘서트 서비스 테스트에서는 콘서트 저장소와 좌석 저장소를 초기화하는 것이 아니라 콘서트 서비스를 초기화하는 것이다.
    @BeforeEach
    void setUp() {
        concertRepository = new ConcertRepository();
        seatRepository = new SeatRepository();
        concertService = new ConcertServiceImpl(concertRepository, seatRepository);
        concertService.initializeConcerts();
    }

    @Test
    void 예약_가능한_콘서트_날짜_조회_성공() {
        // When
        List<String> availableDates = concertService.getAvailableConcertDates();

        // Then
        assertNotNull(availableDates);
        assertFalse(availableDates.isEmpty());
        assertEquals(7, availableDates.size()); // 향후 7일간
        
        // 날짜가 정렬되어 있는지 확인
        for (int i = 1; i < availableDates.size(); i++) {
            assertTrue(availableDates.get(i-1).compareTo(availableDates.get(i)) <= 0);
        }
    }

    @Test
    void 특정_날짜의_예약_가능한_좌석_조회_성공() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();

        // When
        List<String> availableSeats = concertService.getAvailableSeatsByDate(concertDate);

        // Then
        assertNotNull(availableSeats);
        assertEquals(50, availableSeats.size()); // 총 50개 좌석
        
        // 좌석 번호가 1부터 50까지 있는지 확인
        for (int i = 1; i <= 50; i++) {
            assertTrue(availableSeats.contains(String.valueOf(i)));
        }
        
        // 좌석 번호가 정렬되어 있는지 확인
        for (int i = 1; i < availableSeats.size(); i++) {
            int seat1 = Integer.parseInt(availableSeats.get(i-1));
            int seat2 = Integer.parseInt(availableSeats.get(i));
            assertTrue(seat1 <= seat2);
        }
    }

    @Test
    void 존재하지_않는_날짜의_좌석_조회_시_빈_리스트_반환() {
        // Given
        String nonExistentDate = "2025-12-31";

        // When
        List<String> availableSeats = concertService.getAvailableSeatsByDate(nonExistentDate);

        // Then
        assertNotNull(availableSeats);
        assertTrue(availableSeats.isEmpty());
    }

    @Test
    void 잘못된_입력값으로_좌석_조회_실패() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getAvailableSeatsByDate(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getAvailableSeatsByDate("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getAvailableSeatsByDate("   ");
        });
    }

    @Test
    void 콘서트_초기화_후_데이터_정상_생성() {
        // Given - setUp에서 이미 초기화됨

        // When
        List<String> availableDates = concertService.getAvailableConcertDates();
        String firstDate = availableDates.get(0);
        List<String> seats = concertService.getAvailableSeatsByDate(firstDate);

        // Then
        assertNotNull(availableDates);
        assertFalse(availableDates.isEmpty());
        
        assertNotNull(seats);
        assertEquals(50, seats.size());
        
        // 모든 좌석이 예약 가능한 상태인지 확인
        for (String seatId : seats) {
            Optional<Seat> seatOptional = seatRepository.findByConcertDateAndSeatId(firstDate, seatId);
            assertTrue(seatOptional.isPresent());
            assertTrue(seatOptional.get().isAvailable());
        }
    }

    @Test
    void 날짜_범위로_콘서트_조회_성공() {
        // Given
        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate = LocalDate.now().plusDays(3).toString();

        // When
        List<Concert> concerts = concertService.getAvailableConcertsByDateRange(startDate, endDate);

        // Then
        assertNotNull(concerts);
        assertEquals(3, concerts.size()); // 3일간의 콘서트
        
        for (Concert concert : concerts) {
            assertTrue(concert.getDate().compareTo(startDate) >= 0);
            assertTrue(concert.getDate().compareTo(endDate) <= 0);
        }
    }

    @Test
    void 콘서트_좌석_상세_정보_조회_성공() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();
        String concertTitle = "콘서트 1일차";

        // When
        ConcertSeatResponse response = concertService.getConcertSeatDetails(concertDate, concertTitle);

        // Then
        assertNotNull(response);
        assertNotNull(response.getConcertInfo());
        assertEquals(concertDate, response.getConcertInfo().getDate());
        assertEquals(concertTitle, response.getConcertInfo().getTitle());
        assertEquals(50, response.getTotalSeats());
        assertEquals(50, response.getAvailableCount());
        assertEquals(50, response.getAvailableSeats().size());
    }

    @Test
    void 존재하지_않는_콘서트_좌석_조회_실패() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();
        String nonExistentTitle = "존재하지 않는 콘서트";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getConcertSeatDetails(concertDate, nonExistentTitle);
        });
    }

    @Test
    void 잘못된_날짜_형식으로_범위_조회_실패() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getAvailableConcertsByDateRange("2024-13-01", "2024-12-31");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getAvailableConcertsByDateRange("2024-12-01", "2024-11-30");
        });
    }

    @Test
    void 좌석_상태_변경_후_조회_테스트() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        
        // 좌석을 HOLD 상태로 변경
        Optional<Seat> seatOptional = seatRepository.findByConcertDateAndSeatId(concertDate, seatId);
        assertTrue(seatOptional.isPresent());
        Seat seat = seatOptional.get();
        seat.holdSeat("testUser", 5);
        seatRepository.update(seat);

        // When
        ConcertSeatResponse response = concertService.getConcertSeatDetails(concertDate, "콘서트 1일차");

        // Then
        assertNotNull(response);
        assertEquals(49, response.getAvailableCount()); // 1개 좌석이 HOLD 상태
        assertEquals(50, response.getTotalSeats());
        
        // HOLD 상태인 좌석 확인
        boolean foundHoldSeat = response.getAvailableSeats().stream()
            .anyMatch(seatResponse -> seatResponse.getSeatId().equals(seatId) && 
                                    seatResponse.getStatus().equals("HOLD"));
        assertTrue(foundHoldSeat);
    }
}
