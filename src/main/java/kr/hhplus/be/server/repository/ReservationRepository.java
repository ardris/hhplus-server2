package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.Reservation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 예약 정보를 관리하는 인메모리 저장소
 */
public class ReservationRepository {

    private final Map<String, Reservation> reservationStore = new ConcurrentHashMap<>();

    public Reservation save(Reservation reservation) {
        reservationStore.put(reservation.getReservationId(), reservation);
        return reservation;
    }

    public Optional<Reservation> findByReservationId(String reservationId) {
        return Optional.ofNullable(reservationStore.get(reservationId));
    }
    
    public List<Reservation> findByUserId(String userId) {
        List<Reservation> userReservations = new ArrayList<>();
        for (Reservation reservation : reservationStore.values()) {
            if (reservation.getUserId().equals(userId)) {
                userReservations.add(reservation);
            }
        }
        return userReservations;
    }
    
    public List<Reservation> findByConcertDateAndSeatId(String concertDate, String seatId) {
        List<Reservation> reservations = new ArrayList<>();
        for (Reservation reservation : reservationStore.values()) {
            if (reservation.getConcertDate().equals(concertDate) && 
                reservation.getSeatId().equals(seatId)) {
                reservations.add(reservation);
            }
        }
        return reservations;
    }
    
    public List<Reservation> findExpiredHolds() {
        List<Reservation> expiredReservations = new ArrayList<>();
        for (Reservation reservation : reservationStore.values()) {
            if (reservation.isHoldExpired()) {
                expiredReservations.add(reservation);
            }
        }
        return expiredReservations;
    }

    public void update(Reservation reservation) {
        reservationStore.put(reservation.getReservationId(), reservation);
    }
    
    public boolean existsByReservationId(String reservationId) {
        return reservationStore.containsKey(reservationId);
    }
    
    public void deleteByReservationId(String reservationId) {
        reservationStore.remove(reservationId);
    }
}
