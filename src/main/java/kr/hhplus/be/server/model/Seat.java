package kr.hhplus.be.server.model;

import java.time.LocalDateTime;

/**
 * 좌석 정보를 관리하는 도메인 모델
 * 
 * 콘서트 예약 서비스에서 개별 좌석의 상태와 예약 정보를 관리하는 클래스입니다.
 * 좌석의 임시 배정(HOLD), 결제 완료(SOLD), 만료(EXPIRED) 등의 상태 변화를 담당하고,
 * 동시성 문제를 방지하기 위한 좌석 잠금 메커니즘을 제공합니다.
 */
public class Seat {
    
    private String seatId;              // 좌석 고유 식별자 (예: "1", "2", "3", ..., "50")
    private String concertDate;         // 콘서트 날짜 (yyyy-MM-dd 형식)
    private SeatStatus status;          // 좌석 현재 상태 (AVAILABLE, HOLD, SOLD, EXPIRED)
    private String reservedByUserId;    // 좌석을 예약한 사용자 ID (임시 배정 시에만 설정)
    private LocalDateTime holdExpiresAt; // 임시 배정 만료 시간 (5분 후)
    private LocalDateTime createdAt;    // 좌석 생성 시간
    private LocalDateTime updatedAt;    // 좌석 정보 마지막 수정 시간
    
    /**
     * 좌석의 상태를 나타내는 열거형
     * 
     * AVAILABLE: 예약 가능한 상태 (기본 상태)
     * HOLD: 임시 배정된 상태 (결제 대기 중, 5분간 다른 사용자가 예약 불가)
     * SOLD: 판매 완료된 상태 (결제 완료, 영구적으로 예약 불가)
     * EXPIRED: 임시 배정이 만료된 상태 (5분 경과 후 자동으로 AVAILABLE로 변경됨)
     */
    public enum SeatStatus {
        AVAILABLE,      // 예약 가능
        HOLD,          // 임시 배정 (결제 대기)
        SOLD,          // 판매 완료
        EXPIRED        // 임시 배정 만료
    }
    
    /**
     * 좌석 객체를 생성합니다.
     * 
     * @param seatId 좌석 고유 식별자
     * @param concertDate 콘서트 날짜
     * 
     * 이유: 콘서트 등록 시 각 좌석을 Seat 객체로 생성할 때 호출됩니다.
     * 생성 시 status는 기본적으로 AVAILABLE로 설정되어 예약이 가능한 상태가 됩니다.
     * 생성/수정 시간을 현재 시간으로 기록하여 추적 가능하게 합니다.
     */
    // 좌석 객체 생성
    public Seat(String seatId, String concertDate) {
        this.seatId = seatId;
        this.concertDate = concertDate;
        this.status = SeatStatus.AVAILABLE;  // 기본적으로 예약 가능한 상태
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 좌석을 특정 사용자에게 임시 배정합니다.
     * 
     * @param userId 좌석을 예약할 사용자 ID
     * @param holdMinutes 임시 배정 유지 시간 (분 단위)
     * @throws IllegalStateException 좌석이 예약 가능한 상태가 아닐 때
     * 
     * 이유: 사용자가 좌석 예약을 요청했을 때, 결제 전까지 다른 사용자가 해당 좌석을 예약하지 못하도록
     * 임시로 배정하는 기능입니다. 기본적으로 5분간 유지되며, 이 시간 내에 결제가 완료되어야 합니다.
     * 동시성 문제를 방지하여 같은 좌석을 여러 사용자가 동시에 예약하는 것을 막습니다.
     */
    // 좌석 임시 배정
    public void holdSeat(String userId, int holdMinutes) {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("예약 가능한 좌석이 아닙니다.");
        }
        this.status = SeatStatus.HOLD;
        this.reservedByUserId = userId;
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(holdMinutes);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 좌석 예약을 확정합니다 (결제 완료 후).
     * 
     * @throws IllegalStateException 좌석이 임시 배정 상태가 아닐 때
     * 
     * 이유: 사용자가 결제를 완료했을 때 호출되어, 좌석을 영구적으로 해당 사용자에게 배정합니다.
     * HOLD 상태에서 SOLD 상태로 변경하여, 더 이상 다른 사용자가 예약할 수 없게 됩니다.
     * reservedByUserId는 그대로 유지되어 누가 예약했는지 추적할 수 있습니다.
     */
    // 좌석 예약 확정
    public void confirmReservation() {
        if (this.status != SeatStatus.HOLD) {
            throw new IllegalStateException("임시 배정된 좌석이 아닙니다.");
        }
        this.status = SeatStatus.SOLD;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 좌석의 임시 배정을 해제합니다.
     * 
     * 이유: 5분이 경과했거나 사용자가 결제를 취소했을 때 호출됩니다.
     * HOLD 상태의 좌석을 EXPIRED 상태로 변경하고, reservedByUserId와 holdExpiresAt을 초기화합니다.
     * 이후 스케줄러나 정리 작업에서 EXPIRED 상태를 AVAILABLE로 변경하여 다시 예약 가능하게 만듭니다.
     */
    // 좌석 임시 배정 해제
    public void releaseHold() {
        if (this.status == SeatStatus.HOLD) {
            this.status = SeatStatus.EXPIRED;
            this.reservedByUserId = null;
            this.holdExpiresAt = null;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 좌석의 임시 배정이 만료되었는지 확인합니다.
     * 
     * @return true: 만료됨, false: 아직 유효함
     * 
     * 이유: 스케줄러나 정리 작업에서 만료된 임시 배정을 찾아 정리할 때 사용됩니다.
     * HOLD 상태이면서 holdExpiresAt 시간이 현재 시간보다 이전인 경우 만료로 판단합니다.
     * 이 메서드를 통해 주기적으로 만료된 좌석들을 AVAILABLE 상태로 복원할 수 있습니다.
     */
    // 임시 배정 만료 여부 확인
    public boolean isHoldExpired() {
        return this.status == SeatStatus.HOLD && 
               this.holdExpiresAt != null && 
               this.holdExpiresAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * 좌석이 예약 가능한 상태인지 확인합니다.
     * 
     * @return true: 예약 가능, false: 예약 불가
     * 
     * 이유: 사용자가 좌석 예약을 시도하기 전에 해당 좌석이 예약 가능한지 확인할 때 사용됩니다.
     * AVAILABLE 상태인 좌석만 예약할 수 있으며, HOLD, SOLD, EXPIRED 상태는 예약이 불가능합니다.
     * UI에서 좌석 선택 가능 여부를 표시하거나, 서비스 레이어에서 사전 검증할 때 활용됩니다.
     */
    // 좌석 예약 가능 여부 확인
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }
    
    /**
     * 좌석이 특정 사용자에게 임시 배정되어 있는지 확인
     */
    public boolean isHeldByUser(String userId) {
        return this.status == SeatStatus.HOLD && 
               this.reservedByUserId != null && 
               this.reservedByUserId.equals(userId) &&
               !isHoldExpired();
    }
    
    // Getters and Setters
    public String getSeatId() {
        return seatId;
    }
    
    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }
    
    public String getConcertDate() {
        return concertDate;
    }
    
    public void setConcertDate(String concertDate) {
        this.concertDate = concertDate;
    }
    
    public SeatStatus getStatus() {
        return status;
    }
    
    public void setStatus(SeatStatus status) {
        this.status = status;
    }
    
    public String getReservedByUserId() {
        return reservedByUserId;
    }
    
    public void setReservedByUserId(String reservedByUserId) {
        this.reservedByUserId = reservedByUserId;
    }
    
    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }
    
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 좌석 예약 (HOLD 상태로 변경)
     */
    public void reserveSeat(String userId) {
        if (!isAvailable()) {
            throw new IllegalStateException("예약할 수 없는 좌석입니다.");
        }
        this.status = SeatStatus.HOLD;
        this.reservedByUserId = userId;
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(5);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 좌석을 판매 완료 상태로 변경 (결제 완료 시)
     */
    public void sellSeat() {
        if (this.status != SeatStatus.HOLD) {
            throw new IllegalStateException("임시 배정된 좌석이 아닙니다.");
        }
        this.status = SeatStatus.SOLD;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 좌석을 다시 예약 가능 상태로 변경
     */
    public void releaseSeat() {
        this.status = SeatStatus.AVAILABLE;
        this.reservedByUserId = null;
        this.holdExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
}
