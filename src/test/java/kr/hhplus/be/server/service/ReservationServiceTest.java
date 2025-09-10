package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.ReservationException;
import kr.hhplus.be.server.model.Reservation;
import kr.hhplus.be.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 예약 서비스 단위 테스트
 */
class ReservationServiceTest {

    private ReservationService reservationService;
    private QueueService queueService;
    private ConcertService concertService;
    private ReservationRepository reservationRepository;
    private SeatRepository seatRepository;

    @BeforeEach
    void setUp() {
        // 저장소 초기화
        QueueTokenRepository queueTokenRepository = new QueueTokenRepository();
        ConcertRepository concertRepository = new ConcertRepository();
        seatRepository = new SeatRepository();
        reservationRepository = new ReservationRepository();

        // 서비스 초기화
        queueService = new QueueServiceImpl(queueTokenRepository);
        concertService = new ConcertServiceImpl(concertRepository, seatRepository);
        reservationService = new ReservationServiceImpl(reservationRepository, seatRepository, queueService);

        // 콘서트 초기화
        concertService.initializeConcerts();
    }

    @Test
    void 좌석_예약_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        // When
        Reservation reservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // Then
        assertNotNull(reservation);
        assertEquals(userId, reservation.getUserId());
        assertEquals(concertDate, reservation.getConcertDate());
        assertEquals(seatId, reservation.getSeatId());
        assertEquals(ticketPrice, reservation.getTicketPrice());
        assertEquals(Reservation.ReservationStatus.HOLD, reservation.getStatus());
        assertTrue(reservation.isHold());
    }

    @Test
    void 동일_사용자_동일_좌석_재예약_시_기존_예약_반환() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        Reservation firstReservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // When
        Reservation secondReservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // Then
        assertEquals(firstReservation.getReservationId(), secondReservation.getReservationId());
    }

    @Test
    void 이미_예약된_좌석_예약_실패() {
        // Given
        String userId1 = "user123";
        String userId2 = "user456";
        String tokenId1 = queueService.issueToken(userId1);
        String tokenId2 = queueService.issueToken(userId2);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        reservationService.holdSeat(tokenId1, concertDate, seatId, ticketPrice);

        // When & Then
        assertThrows(ReservationException.SeatAlreadyHeldException.class, () -> {
            reservationService.holdSeat(tokenId2, concertDate, seatId, ticketPrice);
        });
    }

    @Test
    void 존재하지_않는_좌석_예약_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "999"; // 존재하지 않는 좌석
        BigDecimal ticketPrice = new BigDecimal("100000");

        // When & Then
        assertThrows(ReservationException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);
        });
    }

    @Test
    void 예약_조회_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        Reservation createdReservation = reservationService.holdSeat(tokenId, concertDate, seatId, ticketPrice);

        // When
        Reservation foundReservation = reservationService.getReservation(tokenId, createdReservation.getReservationId());

        // Then
        assertNotNull(foundReservation);
        assertEquals(createdReservation.getReservationId(), foundReservation.getReservationId());
        assertEquals(userId, foundReservation.getUserId());
    }

    @Test
    void 다른_사용자_예약_조회_실패() {
        // Given
        String userId1 = "user123";
        String userId2 = "user456";
        String tokenId1 = queueService.issueToken(userId1);
        String tokenId2 = queueService.issueToken(userId2);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        Reservation reservation = reservationService.holdSeat(tokenId1, concertDate, seatId, ticketPrice);

        // When & Then
        assertThrows(ReservationException.class, () -> {
            reservationService.getReservation(tokenId2, reservation.getReservationId());
        });
    }

    @Test
    void 사용자_예약_목록_조회_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        BigDecimal ticketPrice = new BigDecimal("100000");

        reservationService.holdSeat(tokenId, concertDate, "1", ticketPrice);
        reservationService.holdSeat(tokenId, concertDate, "2", ticketPrice);

        // When
        var reservations = reservationService.getUserReservations(tokenId);

        // Then
        assertEquals(2, reservations.size());
        for (Reservation reservation : reservations) {
            assertTrue(reservation.getUserId().equals(userId));
        }
    }

    @Test
    void 잘못된_입력값으로_예약_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        String concertDate = LocalDate.now().plusDays(1).toString();
        String seatId = "1";
        BigDecimal ticketPrice = new BigDecimal("100000");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, null, seatId, ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, "", seatId, ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, null, ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, "", ticketPrice);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, seatId, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, seatId, BigDecimal.ZERO);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.holdSeat(tokenId, concertDate, seatId, new BigDecimal("-1000"));
        });
    }
}
