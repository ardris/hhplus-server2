package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.model.ConcertSeatResponse;
import kr.hhplus.be.server.model.Seat;
import kr.hhplus.be.server.repository.ConcertRepository;
import kr.hhplus.be.server.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ConcertService Mock 단위 테스트
 * 의존성을 Mock으로 분리하여 순수한 콘서트 비즈니스 로직만 테스트
 */
@ExtendWith(MockitoExtension.class)
class ConcertServiceMockTest {

    @Mock
    private ConcertRepository concertRepository;
    
    @Mock
    private SeatRepository seatRepository;

    private ConcertService concertService;

    @BeforeEach
    void setUp() {
        concertService = new ConcertServiceImpl(concertRepository, seatRepository);
    }

    @Test
    void 예약_가능한_콘서트_날짜_조회_성공_Mock_테스트() {
        // Given
        String date1 = LocalDate.now().plusDays(1).toString();
        String date2 = LocalDate.now().plusDays(2).toString();
        String date3 = LocalDate.now().plusDays(3).toString();
        
        Concert concert1 = new Concert("concert1", "콘서트 1일차", date1, 50, new BigDecimal("150000"));
        Concert concert2 = new Concert("concert2", "콘서트 2일차", date2, 50, new BigDecimal("120000"));
        Concert concert3 = new Concert("concert3", "콘서트 3일차", date3, 50, new BigDecimal("130000"));
        
        List<Concert> mockConcerts = Arrays.asList(concert1, concert2, concert3);
        
        // Mock 설정
        when(concertRepository.findAllActiveConcerts()).thenReturn(mockConcerts);

        // When
        List<String> availableDates = concertService.getAvailableConcertDates();

        // Then
        assertNotNull(availableDates);
        assertEquals(3, availableDates.size());
        assertTrue(availableDates.contains(date1));
        assertTrue(availableDates.contains(date2));
        assertTrue(availableDates.contains(date3));
        
        // Mock 검증
        verify(concertRepository, times(1)).findAllActiveConcerts();
    }

    @Test
    void 특정_날짜의_예약_가능한_좌석_조회_성공_Mock_테스트() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();
        
        Seat seat1 = new Seat("1", concertDate, Seat.SeatStatus.AVAILABLE);
        Seat seat2 = new Seat("2", concertDate, Seat.SeatStatus.AVAILABLE);
        Seat seat3 = new Seat("3", concertDate, Seat.SeatStatus.AVAILABLE);
        
        List<Seat> mockSeats = Arrays.asList(seat1, seat2, seat3);
        
        // Mock 설정
        when(seatRepository.findByConcertDate(concertDate)).thenReturn(mockSeats);

        // When
        List<String> availableSeats = concertService.getAvailableSeatsByDate(concertDate);

        // Then
        assertNotNull(availableSeats);
        assertEquals(3, availableSeats.size());
        assertTrue(availableSeats.contains("1"));
        assertTrue(availableSeats.contains("2"));
        assertTrue(availableSeats.contains("3"));
        
        // Mock 검증
        verify(seatRepository, times(1)).findByConcertDate(concertDate);
    }

    @Test
    void 존재하지_않는_날짜의_좌석_조회시_빈_리스트_반환_Mock_테스트() {
        // Given
        String nonExistentDate = "2025-12-31";
        
        // Mock 설정: 좌석이 존재하지 않음
        when(seatRepository.findByConcertDate(nonExistentDate)).thenReturn(Arrays.asList());

        // When
        List<String> availableSeats = concertService.getAvailableSeatsByDate(nonExistentDate);

        // Then
        assertNotNull(availableSeats);
        assertTrue(availableSeats.isEmpty());
        
        // Mock 검증
        verify(seatRepository, times(1)).findByConcertDate(nonExistentDate);
    }

    @Test
    void 날짜_범위로_콘서트_조회_성공_Mock_테스트() {
        // Given
        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate = LocalDate.now().plusDays(3).toString();
        
        Concert concert1 = new Concert("concert1", "콘서트 1일차", startDate, 50, new BigDecimal("100000"));
        Concert concert2 = new Concert("concert2", "콘서트 2일차", LocalDate.now().plusDays(2).toString(), 50, new BigDecimal("100000"));
        Concert concert3 = new Concert("concert3", "콘서트 3일차", endDate, 50, new BigDecimal("100000"));
        
        List<Concert> mockConcerts = Arrays.asList(concert1, concert2, concert3);
        
        // Mock 설정
        when(concertRepository.findByDateRange(startDate, endDate)).thenReturn(mockConcerts);

        // When
        List<Concert> concerts = concertService.getAvailableConcertsByDateRange(startDate, endDate);

        // Then
        assertNotNull(concerts);
        assertEquals(3, concerts.size());
        
        // Mock 검증
        verify(concertRepository, times(1)).findByDateRange(startDate, endDate);
    }

    @Test
    void 콘서트_좌석_상세_정보_조회_성공_Mock_테스트() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();
        String concertTitle = "콘서트 1일차";
        
        Concert mockConcert = new Concert("concert1", concertTitle, concertDate, 50, new BigDecimal("100000"));
        
        Seat seat1 = new Seat("1", concertDate, Seat.SeatStatus.AVAILABLE);
        Seat seat2 = new Seat("2", concertDate, Seat.SeatStatus.AVAILABLE);
        Seat seat3 = new Seat("3", concertDate, Seat.SeatStatus.HOLD);
        seat3.holdSeat("user123", 5);
        
        List<Seat> mockSeats = Arrays.asList(seat1, seat2, seat3);
        
        // Mock 설정
        when(concertRepository.findByDateAndTitle(concertDate, concertTitle)).thenReturn(Optional.of(mockConcert));
        when(seatRepository.findByConcertDate(concertDate)).thenReturn(mockSeats);

        // When
        ConcertSeatResponse response = concertService.getConcertSeatDetails(concertDate, concertTitle);

        // Then
        assertNotNull(response);
        assertNotNull(response.getConcertInfo());
        assertEquals(concertDate, response.getConcertInfo().getDate());
        assertEquals(concertTitle, response.getConcertInfo().getTitle());
        assertEquals(3, response.getTotalSeats());
        assertEquals(2, response.getAvailableCount()); // 2개 좌석이 AVAILABLE
        assertEquals(3, response.getAvailableSeats().size());
        
        // Mock 검증
        verify(concertRepository, times(1)).findByDateAndTitle(concertDate, concertTitle);
        verify(seatRepository, times(1)).findByConcertDate(concertDate);
    }

    @Test
    void 존재하지_않는_콘서트_좌석_조회_실패_Mock_테스트() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();
        String nonExistentTitle = "존재하지 않는 콘서트";
        
        // Mock 설정: 콘서트가 존재하지 않음
        when(concertRepository.findByDateAndTitle(concertDate, nonExistentTitle)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getConcertSeatDetails(concertDate, nonExistentTitle);
        });
        
        // Mock 검증
        verify(concertRepository, times(1)).findByDateAndTitle(concertDate, nonExistentTitle);
        verify(seatRepository, never()).findByConcertDate(anyString());
    }

    @Test
    void 잘못된_입력값_좌석_조회_실패_Mock_테스트() {
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
        
        // Mock 검증: 잘못된 입력으로는 저장소가 호출되지 않아야 함
        verify(seatRepository, never()).findByConcertDate(anyString());
    }

    @Test
    void 잘못된_날짜_형식_범위_조회_실패_Mock_테스트() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getAvailableConcertsByDateRange("2024-13-01", "2024-12-31");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            concertService.getAvailableConcertsByDateRange("2024-12-01", "2024-11-30");
        });
        
        // Mock 검증: 잘못된 입력으로는 저장소가 호출되지 않아야 함
        verify(concertRepository, never()).findByDateRange(anyString(), anyString());
    }

    @Test
    void 콘서트_초기화_후_데이터_정상_생성_Mock_테스트() {
        // Given
        String concertDate = LocalDate.now().plusDays(1).toString();
        Concert mockConcert = new Concert("concert1", "콘서트 1일차", concertDate, 50, new BigDecimal("100000"));
        
        // Mock 설정
        when(concertRepository.save(any(Concert.class))).thenAnswer(invocation -> {
            Concert concert = invocation.getArgument(0);
            return concert;
        });
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> {
            Seat seat = invocation.getArgument(0);
            return seat;
        });

        // When
        concertService.initializeConcerts();

        // Then
        // Mock 검증: 콘서트와 좌석이 저장되었는지 확인
        verify(concertRepository, atLeastOnce()).save(any(Concert.class));
        verify(seatRepository, atLeastOnce()).save(any(Seat.class));
    }
}
