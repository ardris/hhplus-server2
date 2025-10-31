package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.domain.port.UserPort;
import kr.hhplus.be.server.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MakeReservationUseCase Mock 단위 테스트
 * Use Case 기반 아키텍처에 맞춘 테스트
 */
@ExtendWith(MockitoExtension.class)
class MakeReservationUseCaseTest {

    @Mock
    private ReservationRepositoryPort reservationRepository;
    
    @Mock
    private SeatPort seatPort;
    
    @Mock
    private UserPort userPort;

    private MakeReservationUseCase makeReservationUseCase;

    @BeforeEach
    void setUp() {
        makeReservationUseCase = new MakeReservationUseCase(
            reservationRepository,
            seatPort,
            userPort
        );
    }

    @Test
    void 예약_생성_성공() {
        // Given
        String userId = "user123";
        String seatId = "seat001";
        String concertId = "concert001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);

        MakeReservationUseCase.MakeReservationCommand command = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);

        // Mock 사용자 존재
        User mockUser = new User(userId);
        when(userPort.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // Mock 좌석 사용 가능
        when(seatPort.isSeatAvailable(seatId)).thenReturn(true);
        
        // Mock 예약 저장
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            return reservation;
        });

        // When
        Reservation result = makeReservationUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(concertId, result.getPerformanceId());
        assertEquals(seatId, result.getSeatId());
        assertEquals(ticketPrice, result.getTicketPrice());
        
        // 검증: 좌석 임시 배정 호출
        verify(seatPort).holdSeat(eq(seatId), anyString());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void 사용자가_존재하지_않으면_예약_실패() {
        // Given
        String userId = "nonexistent";
        String seatId = "seat001";
        String concertId = "concert001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);

        MakeReservationUseCase.MakeReservationCommand command = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);

        when(userPort.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> makeReservationUseCase.execute(command)
        );
        
        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
        
        // 좌석 배정 시도하지 않음
        verify(seatPort, never()).holdSeat(anyString(), anyString());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 좌석이_사용_불가능하면_예약_실패() {
        // Given
        String userId = "user123";
        String seatId = "seat001";
        String concertId = "concert001";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);

        MakeReservationUseCase.MakeReservationCommand command = 
            new MakeReservationUseCase.MakeReservationCommand(userId, seatId, concertId, ticketPrice);

        User mockUser = new User(userId);
        when(userPort.findById(userId)).thenReturn(Optional.of(mockUser));
        when(seatPort.isSeatAvailable(seatId)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> makeReservationUseCase.execute(command)
        );
        
        assertTrue(exception.getMessage().contains("좌석을 예약할 수 없습니다"));
        
        // 좌석 배정 시도하지 않음
        verify(seatPort, never()).holdSeat(anyString(), anyString());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void 여러_예약_생성_테스트() {
        // Given
        String userId = "user123";
        User mockUser = new User(userId);
        when(userPort.findById(userId)).thenReturn(Optional.of(mockUser));
        when(seatPort.isSeatAvailable(anyString())).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - 3개의 다른 좌석 예약
        for (int i = 1; i <= 3; i++) {
            String seatId = "seat00" + i;
            MakeReservationUseCase.MakeReservationCommand command = 
                new MakeReservationUseCase.MakeReservationCommand(
                    userId, seatId, "concert001", BigDecimal.valueOf(50000)
                );
            
            Reservation result = makeReservationUseCase.execute(command);
            
            // Then
            assertNotNull(result);
            assertEquals(seatId, result.getSeatId());
        }

        // 총 3번 호출 검증
        verify(seatPort, times(3)).holdSeat(anyString(), anyString());
        verify(reservationRepository, times(3)).save(any(Reservation.class));
    }
}

