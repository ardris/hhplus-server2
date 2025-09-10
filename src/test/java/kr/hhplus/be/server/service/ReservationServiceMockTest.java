package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.ReservationException;
import kr.hhplus.be.server.model.QueueToken;
import kr.hhplus.be.server.model.Reservation;
import kr.hhplus.be.server.model.Seat;
import kr.hhplus.be.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReservationService Mock 단위 테스트
 * 의존성을 Mock으로 분리하여 순수한 예약 비즈니스 로직만 테스트
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceMockTest {

    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private SeatRepository seatRepository;
    
    @Mock
    private ConcertRepository concertRepository;
    
    @Mock
    private QueueService queueService;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(reservationRepository, seatRepository, concertRepository, queueService);
    }

    @Test
    void 좌석_예약_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Seat mockSeat = new Seat(seatId, concertDate, Seat.SeatStatus.AVAILABLE);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(seatRepository.findByConcertDateAndSeatId(concertDate, seatId)).thenReturn(Optional.of(mockSeat));
        when(reservationRepository.findByUserIdAndConcertDateAndSeatId(userId, concertDate, seatId))
            .thenReturn(Optional.empty());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            return reservation;
        });
        when(seatRepository.update(any(Seat.class))).thenAnswer(invocation -> {
            Seat seat = invocation.getArgument(0);
            return seat;
        });

        // When
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // Then
        assertNotNull(reservation);
        assertEquals(userId, reservation.getUserId());
        assertEquals(concertDate, reservation.getConcertDate());
        assertEquals(seatId, reservation.getSeatId());
        assertEquals(ticketPrice, reservation.getTicketPrice());
        assertEquals(Reservation.ReservationStatus.HOLD, reservation.getStatus());
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(seatRepository, times(1)).findByConcertDateAndSeatId(concertDate, seatId);
        verify(reservationRepository, times(1)).findByUserIdAndConcertDateAndSeatId(userId, concertDate, seatId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(seatRepository, times(1)).update(any(Seat.class));
    }

    @Test
    void 이미_예약된_좌석_예약_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Seat mockSeat = new Seat(seatId, concertDate, Seat.SeatStatus.HOLD);
        mockSeat.holdSeat("otherUser", 5);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(seatRepository.findByConcertDateAndSeatId(concertDate, seatId)).thenReturn(Optional.of(mockSeat));

        // When & Then
        assertThrows(ReservationException.SeatAlreadyHeldException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);
        });
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(seatRepository, times(1)).findByConcertDateAndSeatId(concertDate, seatId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_좌석_예약_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "999";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정: 좌석이 존재하지 않음
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(seatRepository.findByConcertDateAndSeatId(concertDate, seatId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ReservationException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);
        });
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(seatRepository, times(1)).findByConcertDateAndSeatId(concertDate, seatId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 동일_사용자_동일_좌석_재예약시_기존_예약_반환_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Reservation existingReservation = new Reservation(userId, concertDate, seatId, ticketPrice);
        
        // Mock 설정: 기존 예약이 존재함
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByUserIdAndConcertDateAndSeatId(userId, concertDate, seatId))
            .thenReturn(Optional.of(existingReservation));

        // When
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // Then
        assertEquals(existingReservation.getReservationId(), reservation.getReservationId());
        
        // Mock 검증: 새로운 예약이 저장되지 않았는지 확인
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByUserIdAndConcertDateAndSeatId(userId, concertDate, seatId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 예약_조회_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String reservationId = "reservation-123";
        Reservation mockReservation = new Reservation(userId, "2024-12-01", "1", new BigDecimal("100000"));
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByReservationId(reservationId)).thenReturn(Optional.of(mockReservation));

        // When
        Reservation reservation = reservationService.getReservation(tokenId, reservationId);

        // Then
        assertNotNull(reservation);
        assertEquals(reservationId, reservation.getReservationId());
        assertEquals(userId, reservation.getUserId());
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByReservationId(reservationId);
    }

    @Test
    void 다른_사용자_예약_조회_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String otherUserId = "other-user";
        String reservationId = "reservation-123";
        Reservation mockReservation = new Reservation(otherUserId, "2024-12-01", "1", new BigDecimal("100000"));
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByReservationId(reservationId)).thenReturn(Optional.of(mockReservation));

        // When & Then
        assertThrows(ReservationException.class, () -> {
            reservationService.getReservation(tokenId, reservationId);
        });
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByReservationId(reservationId);
    }

    @Test
    void 잘못된_입력값_예약_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, null, "1", ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, "", "1", ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, "2024-12-01", null, ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, "2024-12-01", "", ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, "2024-12-01", "1", null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, "2024-12-01", "1", BigDecimal.ZERO);
        });
        
        // Mock 검증: 잘못된 입력으로는 저장소가 호출되지 않아야 함
        verify(queueService, never()).validateToken(anyString());
        verify(seatRepository, never()).findByConcertDateAndSeatId(anyString(), anyString());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
