package kr.hhplus.be.server.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 예약 도메인 엔티티
 * 
 * 클린아키텍처의 도메인 레이어에서 예약 관련 비즈니스 규칙을 담고 있어.
 * JPA나 Spring 같은 외부 의존성 없이 순수한 Java 객체로 구현했어.
 */
public class Reservation {
    private String reservationId;
    private String userId;
    private String concertId;
    private String seatId;
    private BigDecimal ticketPrice;
    private ReservationStatus status;
    private LocalDateTime holdExpiresAt;
    private LocalDateTime createdAt;
    private String paymentId;
    private LocalDateTime paidAt;

    // 생성자
    public Reservation(String reservationId, String userId, String concertId, 
                      String seatId, BigDecimal ticketPrice) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.concertId = concertId;
        this.seatId = seatId;
        this.ticketPrice = ticketPrice;
        this.status = ReservationStatus.HOLD;
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(5);
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 비즈니스 로직 1: 좌석 예약
     * - 도메인 규칙: AVAILABLE 상태에서만 예약 가능
     */
    public void hold() {
        if (this.status != ReservationStatus.AVAILABLE) {
            throw new IllegalStateException("좌석을 예약할 수 없습니다.");
        }
        this.status = ReservationStatus.HOLD;
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(5);
    }

    /**
     * 비즈니스 로직 2: 결제 완료
     * - 도메인 규칙: HOLD 상태에서만 결제 가능
     */
    public void completePayment(String paymentId) {
        if (!canBePaid()) {
            throw new IllegalStateException("결제할 수 없는 예약입니다.");
        }
        this.status = ReservationStatus.PAID;
        this.paymentId = paymentId;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 비즈니스 로직 3: 예약 취소
     * - 도메인 규칙: 결제된 예약은 취소 불가
     */
    public void cancel() {
        if (this.status == ReservationStatus.PAID) {
            throw new IllegalStateException("이미 결제된 예약은 취소할 수 없습니다.");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    /**
     * 비즈니스 로직 4: 만료 확인
     * - 도메인 규칙: 5분 후 만료
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.holdExpiresAt);
    }

    /**
     * 비즈니스 로직 5: 결제 가능 여부
     * - 도메인 규칙: HOLD 상태이고 만료되지 않아야 함
     */
    public boolean canBePaid() {
        return this.status == ReservationStatus.HOLD && !isExpired();
    }

    /**
     * 정적 팩토리 메서드
     * - 도메인 규칙: 예약 ID 자동 생성
     */
    public static Reservation create(String userId, String concertId, String seatId, BigDecimal ticketPrice) {
        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8);
        return new Reservation(reservationId, userId, concertId, seatId, ticketPrice);
    }

    // Getters
    public String getReservationId() { return reservationId; }
    public String getUserId() { return userId; }
    public String getConcertId() { return concertId; }
    public String getSeatId() { return seatId; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getPaymentId() { return paymentId; }
    public LocalDateTime getPaidAt() { return paidAt; }
}

/**
 * 예약 상태 열거형
 * - 도메인 규칙: 예약의 가능한 상태들
 */
public enum ReservationStatus {
    AVAILABLE,    // 예약 가능
    HOLD,         // 임시 배정 (5분)
    PAID,         // 결제 완료
    EXPIRED,      // 만료됨
    CANCELLED,    // 취소됨
    REFUNDED,     // 환불됨
    COMPLETED     // 완료됨
}