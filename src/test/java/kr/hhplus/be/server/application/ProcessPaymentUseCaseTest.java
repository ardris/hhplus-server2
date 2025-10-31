package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.domain.port.PaymentRepositoryPort;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.model.PaymentStatus;
import kr.hhplus.be.server.service.UserService;
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
 * ProcessPaymentUseCase Mock 단위 테스트
 * Use Case 기반 아키텍처에 맞춘 테스트
 */
@ExtendWith(MockitoExtension.class)
class ProcessPaymentUseCaseTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;
    
    @Mock
    private ReservationRepositoryPort reservationRepository;
    
    @Mock
    private UserService userService;

    private ProcessPaymentUseCase processPaymentUseCase;

    @BeforeEach
    void setUp() {
        processPaymentUseCase = new ProcessPaymentUseCase(
            paymentRepository,
            reservationRepository,
            userService
        );
    }

    @Test
    void 결제_처리_성공() {
        // Given
        String reservationId = "RES-001";
        String userId = "user123";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);

        ProcessPaymentUseCase.ProcessPaymentCommand command = 
            new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);

        // Mock 예약 정보
        Reservation mockReservation = Reservation.create(
            userId, 
            "concert001", 
            "seat001", 
            "A", 
            ticketPrice
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));

        // Mock 결제 저장
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return payment;
        });

        // Mock 사용자 잔액 차감 (void 메서드)
        doNothing().when(userService).deductBalance(userId, ticketPrice);

        // When
        ProcessPaymentUseCase.PaymentResult result = processPaymentUseCase.execute(command);

        // Then
        assertNotNull(result);
        assertNotNull(result.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccess());
        assertEquals("결제가 완료되었습니다.", result.getMessage());
        
        // 검증: 잔액 차감 및 결제 저장 호출
        verify(userService).deductBalance(userId, ticketPrice);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void 예약이_존재하지_않으면_결제_실패() {
        // Given
        String reservationId = "nonexistent";
        ProcessPaymentUseCase.ProcessPaymentCommand command = 
            new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> processPaymentUseCase.execute(command)
        );
        
        assertEquals("예약을 찾을 수 없습니다.", exception.getMessage());
        
        // 잔액 차감 및 결제 저장 시도하지 않음
        verify(userService, never()).deductBalance(anyString(), any(BigDecimal.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void 잔액_부족_시_결제_실패() {
        // Given
        String reservationId = "RES-001";
        String userId = "user123";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);

        ProcessPaymentUseCase.ProcessPaymentCommand command = 
            new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);

        Reservation mockReservation = Reservation.create(
            userId, 
            "concert001", 
            "seat001", 
            "A", 
            ticketPrice
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));

        // Mock 잔액 부족 예외
        doThrow(new RuntimeException("잔액이 부족합니다."))
            .when(userService).deductBalance(userId, ticketPrice);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> processPaymentUseCase.execute(command)
        );
        
        assertEquals("잔액이 부족합니다.", exception.getMessage());
        
        // 결제 저장은 시도하지 않음
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void 여러_결제_처리_테스트() {
        // Given
        String userId = "user123";
        BigDecimal ticketPrice = BigDecimal.valueOf(50000);
        
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(userService).deductBalance(anyString(), any(BigDecimal.class));

        // When - 3개의 다른 예약에 대한 결제
        for (int i = 1; i <= 3; i++) {
            String reservationId = "RES-00" + i;
            
            Reservation mockReservation = Reservation.create(
                userId, 
                "concert001", 
                "seat00" + i, 
                "A", 
                ticketPrice
            );
            when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
            
            ProcessPaymentUseCase.ProcessPaymentCommand command = 
                new ProcessPaymentUseCase.ProcessPaymentCommand(reservationId);
            
            ProcessPaymentUseCase.PaymentResult result = processPaymentUseCase.execute(command);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
        }

        // 총 3번 호출 검증
        verify(userService, times(3)).deductBalance(anyString(), any(BigDecimal.class));
        verify(paymentRepository, times(3)).save(any(Payment.class));
    }
}

