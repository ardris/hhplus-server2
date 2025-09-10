package kr.hhplus.be.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 예약 정보를 관리하는 도메인 모델
 *
 * 콘서트 예약 서비스에서 사용자의 좌석 예약 정보와 상태를 관리하는 클래스입니다. 예약 생성부터 결제 완료까지의 전체 라이프사이클을
 * 추적하고, 임시 배정(HOLD) 상태의 만료 시간을 관리하여 자동 정리 기능을 제공합니다.
 */
public class Reservation {

    private String reservationId;        // 예약 고유 식별자
    private String userId;               // 예약한 사용자 ID
    private String concertDate;          // 콘서트 날짜 (yyyy-MM-dd 형식)
    private String seatId;               // 좌석 번호 ("1" ~ "50")
    private BigDecimal ticketPrice;      // 티켓 가격 (BigDecimal 사용 이유: 정확한 금액 계산을 위해)
    private ReservationStatus status;    // 예약 현재 상태 (HOLD, PAID, EXPIRED, CANCELLED)
    private LocalDateTime createdAt;     // 예약 생성 시간
    private LocalDateTime holdExpiresAt; // 임시 배정 만료 시간 (5분 후)
    private LocalDateTime paidAt;        // 결제 완료 시간
    private String paymentId;            // 결제 고유 식별자 (결제 완료 시에만 설정)

    /**
     * 예약의 상태를 나타내는 이넘크랠쓰
     *
     * HOLD: 임시 배정된 상태 (결제 대기 중, 5분간 유효) PAID: 결제 완료된 상태 (예약 확정, 좌석 소유권 확보)
     * EXPIRED: 임시 배정이 만료된 상태 (5분 경과 후 자동으로 변경됨) CANCELLED: 사용자가 취소한 상태 (수동으로
     * 설정됨)
     */
    public enum ReservationStatus {
        HOLD, // 임시 배정 (결제 대기)
        PAID, // 결제 완료
        EXPIRED, // 임시 배정 만료
        CANCELLED   // 취소됨
    }

    /**
     * 예약 객체를 생성합니다.
     *
     * @param reservationId 예약 고유 식별자
     * @param userId 예약한 사용자 ID
     * @param concertDate 콘서트 날짜
     * @param seatId 좌석 번호
     * @param ticketPrice 티켓 가격
     *
     * 이유: 사용자가 좌석 예약을 요청했을 때 호출됩니다. 생성 시 status는 HOLD로 설정되어 임시 배정 상태가 되고,
     * holdExpiresAt은 현재 시간에서 5분 후로 설정되어 자동 만료 시간을 관리합니다. 이 5분 동안 사용자는 결제를 완료해야
     * 하며, 그렇지 않으면 자동으로 만료됩니다.
     */
    // 예약 객체 생성
    public Reservation(String reservationId, String userId, String concertDate, String seatId, BigDecimal ticketPrice) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.concertDate = concertDate;
        this.seatId = seatId;
        this.ticketPrice = ticketPrice;
        this.status = ReservationStatus.HOLD;  // 기본적으로 임시 배정 상태
        this.createdAt = LocalDateTime.now();
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(5); // 5분 임시 배정
    }

    /**
     * 예약의 결제를 완료 처리합니다.
     *
     * @param paymentId 결제 고유 식별자
     * @throws IllegalStateException 예약이 HOLD 상태가 아닐 때
     *
     * 이유: 사용자가 결제를 완료했을 때 호출되어, 예약을 확정 상태로 변경합니다. HOLD 상태에서 PAID 상태로 변경하여, 더 이상
     * 만료되지 않는 영구적인 예약이 됩니다. paymentId와 paidAt을 기록하여 결제 정보를 추적할 수 있게 합니다. 이 시점에서
     * 좌석의 소유권이 해당 사용자에게 확정됩니다.
     */
    // 예약 결제 완료 처리
    public void confirmPayment(String paymentId) {
        if (this.status != ReservationStatus.HOLD) {
            throw new IllegalStateException("결제 가능한 상태가 아닙니다.");
        }
        this.status = ReservationStatus.PAID;
        this.paymentId = paymentId;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 예약의 임시 배정을 만료 처리합니다.
     *
     * 이유: 5분이 경과했거나 스케줄러에 의해 만료된 예약을 정리할 때 호출됩니다. HOLD 상태의 예약을 EXPIRED 상태로
     * 변경하여, 더 이상 결제할 수 없게 만듭니다. 이렇게 만료된 예약은 나중에 정리 작업에서 삭제되거나, 좌석을 다시 예약 가능하게
     * 만듭니다.
     */
    // 예약 임시 배정 만료 처리
    public void expireHold() {
        if (this.status == ReservationStatus.HOLD) {
            this.status = ReservationStatus.EXPIRED;
        }
    }

    /**
     * 예약의 임시 배정이 만료되었는지 확인합니다.
     *
     * @return true: 만료됨, false: 아직 유효함
     *
     * 이유: 스케줄러나 정리 작업에서 만료된 예약을 찾아 정리할 때 사용됩니다. HOLD 상태이면서 holdExpiresAt 시간이 현재
     * 시간보다 이전인 경우 만료로 판단합니다. 이 메서드를 통해 주기적으로 만료된 예약들을 EXPIRED 상태로 변경할 수 있습니다.
     */
    // 예약 임시 배정 만료 여부 확인
    public boolean isHoldExpired() {
        return this.status == ReservationStatus.HOLD
                && this.holdExpiresAt != null
                && this.holdExpiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 예약이 결제 완료된 상태인지 확인합니다.
     *
     * @return true: 결제 완료됨, false: 그렇지 않음
     *
     * 이유: 예약의 최종 상태를 확인할 때 사용됩니다. PAID 상태인 예약은 확정된 예약으로, 좌석 소유권이 확보된 상태입니다.
     * 사용자에게 예약 완료를 알리거나, 좌석 배정 상태를 확인할 때 활용됩니다.
     */
    // 예약 결제 완료 여부 확인
    public boolean isPaid() {
        return this.status == ReservationStatus.PAID;
    }

    /**
     * 예약이 임시 배정 상태인지 확인합니다.
     *
     * @return true: 임시 배정 상태, false: 그렇지 않음
     *
     * 이유: 사용자가 결제를 진행할 수 있는 상태인지 확인할 때 사용됩니다. HOLD 상태인 예약만 결제가 가능하며, EXPIRED나
     * CANCELLED 상태는 결제할 수 없습니다. UI에서 결제 버튼 활성화/비활성화를 결정하거나, 결제 API에서 사전 검증할 때
     * 활용됩니다.
     */
    // 예약 임시 배정 상태 여부 확인
    public boolean isHold() {
        return this.status == ReservationStatus.HOLD;
    }

    // Getters and Setters
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConcertDate() {
        return concertDate;
    }

    public void setConcertDate(String concertDate) {
        this.concertDate = concertDate;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
