package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 예약 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 * Domain Entity와 분리하여 기술적 의존성을 격리했습니다.
 */
@Entity
@Table(name = "reservation_master_info")
public class ReservationEntity {
    
    @Id
    @Column(name = "reservation_id")
    private String reservationId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "performance_id", nullable = false)
    private String performanceId;
    
    @Column(name = "seat_id", nullable = false)
    private String seatId;
    
    @Column(name = "seat_grade", nullable = false)
    private String seatGrade;
    
    @Column(name = "ticket_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus status;
    
    @Column(name = "hold_expires_at", nullable = false)
    private LocalDateTime holdExpiresAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Domain Entity로 변환
     */
    public Reservation toDomain() {
        return new Reservation(
            this.reservationId,
            this.userId,
            this.performanceId,
            this.seatId,
            this.seatGrade,
            this.ticketPrice
        );
    }
    
    /**
     * Domain Entity로부터 생성
     */
    public static ReservationEntity fromDomain(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();
        entity.reservationId = reservation.getReservationId();
        entity.userId = reservation.getUserId();
        entity.performanceId = reservation.getPerformanceId(); // Domain에서 performanceId 사용
        entity.seatId = reservation.getSeatId();
        entity.seatGrade = reservation.getSeatGrade();
        entity.ticketPrice = reservation.getTicketPrice();
        entity.status = reservation.getStatus();
        entity.holdExpiresAt = reservation.getHoldExpiresAt();
        entity.createdAt = reservation.getCreatedAt();
        entity.paymentId = reservation.getPaymentId();
        entity.paidAt = reservation.getPaidAt();
        return entity;
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

    public String getPerformanceId() {
        return performanceId;
    }
    
    public void setPerformanceId(String performanceId) {
        this.performanceId = performanceId;
    }

    public String getSeatId() {
        return seatId;
    }
    
    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getSeatGrade() {
        return seatGrade;
    }
    
    public void setSeatGrade(String seatGrade) {
        this.seatGrade = seatGrade;
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

    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
