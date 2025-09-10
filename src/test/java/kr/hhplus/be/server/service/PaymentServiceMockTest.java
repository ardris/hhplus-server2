package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.PaymentException;
import kr.hhplus.be.server.exception.ReservationException;
import kr.hhplus.be.server.model.*;
import kr.hhplus.be.server.repository.*;
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
 * PaymentService Mock 단위 테스트
 * 의존성을 Mock으로 분리하여 순수한 결제 비즈니스 로직만 테스트
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceMockTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private SeatRepository seatRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private QueueService queueService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, reservationRepository, seatRepository, 
                                              transactionRepository, userService, queueService);
    }

    @Test
    void 결제_처리_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String reservationId = "reservation-123";
        String concertDate = "2024-12-01";
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Reservation mockReservation = new Reservation(userId, concertDate, seatId, ticketPrice);
        Seat mockSeat = new Seat(seatId, concertDate, Seat.SeatStatus.HOLD);
        mockSeat.holdSeat(userId, 5);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByReservationId(reservationId)).thenReturn(Optional.of(mockReservation));
        when(seatRepository.findByConcertDateAndSeatId(concertDate, seatId)).thenReturn(Optional.of(mockSeat));
        when(userService.hasSufficientBalance(userId, ticketPrice)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return payment;
        });
        when(reservationRepository.update(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            return reservation;
        });
        when(seatRepository.update(any(Seat.class))).thenAnswer(invocation -> {
            Seat seat = invocation.getArgument(0);
            return seat;
        });

        // When
        Payment payment = paymentService.processPayment(tokenId, reservationId);

        // Then
        assertNotNull(payment);
        assertEquals(userId, payment.getUserId());
        assertEquals(reservationId, payment.getReservationId());
        assertEquals(ticketPrice, payment.getAmount());
        assertTrue(payment.isCompleted());
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByReservationId(reservationId);
        verify(seatRepository, times(1)).findByConcertDateAndSeatId(concertDate, seatId);
        verify(userService, times(1)).hasSufficientBalance(userId, ticketPrice);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(reservationRepository, times(1)).update(any(Reservation.class));
        verify(seatRepository, times(1)).update(any(Seat.class));
    }

    @Test
    void 잔액_부족시_결제_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String reservationId = "reservation-123";
        String concertDate = "2024-12-01";
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Reservation mockReservation = new Reservation(userId, concertDate, seatId, ticketPrice);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByReservationId(reservationId)).thenReturn(Optional.of(mockReservation));
        when(userService.hasSufficientBalance(userId, ticketPrice)).thenReturn(false);

        // When & Then
        assertThrows(PaymentException.InsufficientBalanceException.class, () -> {
            paymentService.processPayment(tokenId, reservationId);
        });
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByReservationId(reservationId);
        verify(userService, times(1)).hasSufficientBalance(userId, ticketPrice);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void 다른_사용자_예약_결제_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String otherUserId = "other-user";
        String reservationId = "reservation-123";
        String concertDate = "2024-12-01";
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Reservation mockReservation = new Reservation(otherUserId, concertDate, seatId, ticketPrice);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByReservationId(reservationId)).thenReturn(Optional.of(mockReservation));

        // When & Then
        assertThrows(ReservationException.class, () -> {
            paymentService.processPayment(tokenId, reservationId);
        });
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByReservationId(reservationId);
        verify(userService, never()).hasSufficientBalance(anyString(), any(BigDecimal.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void 존재하지_않는_예약_결제_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String reservationId = "non-existent-reservation";
        
        QueueToken mockToken = new QueueToken(tokenId, "user123", 8);
        
        // Mock 설정: 예약이 존재하지 않음
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ReservationException.class, () -> {
            paymentService.processPayment(tokenId, reservationId);
        });
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByReservationId(reservationId);
        verify(userService, never()).hasSufficientBalance(anyString(), any(BigDecimal.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void 거래_내역_포함_결제_처리_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String reservationId = "reservation-123";
        String concertDate = "2024-12-01";
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Reservation mockReservation = new Reservation(userId, concertDate, seatId, ticketPrice);
        Seat mockSeat = new Seat(seatId, concertDate, Seat.SeatStatus.HOLD);
        mockSeat.holdSeat(userId, 5);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(reservationRepository.findByReservationId(reservationId)).thenReturn(Optional.of(mockReservation));
        when(seatRepository.findByConcertDateAndSeatId(concertDate, seatId)).thenReturn(Optional.of(mockSeat));
        when(userService.hasSufficientBalance(userId, ticketPrice)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return payment;
        });
        when(reservationRepository.update(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            return reservation;
        });
        when(seatRepository.update(any(Seat.class))).thenAnswer(invocation -> {
            Seat seat = invocation.getArgument(0);
            return seat;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });

        // When
        Payment payment = paymentService.processPaymentWithTransaction(tokenId, reservationId);

        // Then
        assertNotNull(payment);
        assertEquals(userId, payment.getUserId());
        assertEquals(reservationId, payment.getReservationId());
        assertEquals(ticketPrice, payment.getAmount());
        assertTrue(payment.isCompleted());
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(reservationRepository, times(1)).findByReservationId(reservationId);
        verify(seatRepository, times(1)).findByConcertDateAndSeatId(concertDate, seatId);
        verify(userService, times(1)).hasSufficientBalance(userId, ticketPrice);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(reservationRepository, times(1)).update(any(Reservation.class));
        verify(seatRepository, times(1)).update(any(Seat.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void 결제_조회_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String paymentId = "payment-123";
        Payment mockPayment = new Payment(userId, "reservation-123", new BigDecimal("100000"));
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(mockPayment));

        // When
        Payment payment = paymentService.getPayment(tokenId, paymentId);

        // Then
        assertNotNull(payment);
        assertEquals(paymentId, payment.getPaymentId());
        assertEquals(userId, payment.getUserId());
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
    }

    @Test
    void 다른_사용자_결제_조회_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        String otherUserId = "other-user";
        String paymentId = "payment-123";
        Payment mockPayment = new Payment(otherUserId, "reservation-123", new BigDecimal("100000"));
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(mockPayment));

        // When & Then
        assertThrows(PaymentException.class, () -> {
            paymentService.getPayment(tokenId, paymentId);
        });
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
    }

    @Test
    void 잘못된_입력값_결제_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(tokenId, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(tokenId, "");
        });
        
        // Mock 검증: 잘못된 입력으로는 저장소가 호출되지 않아야 함
        verify(queueService, never()).validateToken(anyString());
        verify(reservationRepository, never()).findByReservationId(anyString());
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
