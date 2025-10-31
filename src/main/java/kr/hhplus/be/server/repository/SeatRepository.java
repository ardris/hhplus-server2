package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.Seat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 좌석 정보를 관리하는 인메모리 저장소
 */
public class SeatRepository {
    
    private final Map<String, Seat> seatStore = new ConcurrentHashMap<>();
    
    public Seat save(Seat seat) {
        String key = generateKey(seat.getConcertDate(), seat.getSeatId());
        seatStore.put(key, seat);
        return seat;
    }
    
    public Optional<Seat> findByConcertDateAndSeatId(String concertDate, String seatId) {
        String key = generateKey(concertDate, seatId);
        return Optional.ofNullable(seatStore.get(key));
    }
    
    public List<Seat> findByConcertDate(String concertDate) {
        List<Seat> seats = new ArrayList<>();
        for (Seat seat : seatStore.values()) {
            if (seat.getConcertDate().equals(concertDate)) {
                seats.add(seat);
            }
        }
        return seats;
    }
    
    public List<Seat> findAvailableSeatsByConcertDate(String concertDate) {
        List<Seat> availableSeats = new ArrayList<>();
        for (Seat seat : findByConcertDate(concertDate)) {
            if (seat.isAvailable()) {
                availableSeats.add(seat);
            }
        }
        return availableSeats;
    }
    
    public List<String> getAvailableSeatIdsByConcertDate(String concertDate) {
        List<String> seatIds = new ArrayList<>();
        for (Seat seat : findAvailableSeatsByConcertDate(concertDate)) {
            seatIds.add(seat.getSeatId());
        }
        
        Collections.sort(seatIds, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                try {
                    return Integer.parseInt(a) - Integer.parseInt(b);
                } catch (NumberFormatException ex) {
                    return a.compareTo(b);
                }
            }
        });
        
        return seatIds;
    }
    
    public void update(Seat seat) {
        String key = generateKey(seat.getConcertDate(), seat.getSeatId());
        seatStore.put(key, seat);
    }
    
    public boolean existsByConcertDateAndSeatId(String concertDate, String seatId) {
        String key = generateKey(concertDate, seatId);
        return seatStore.containsKey(key);
    }
    
    public void deleteByConcertDateAndSeatId(String concertDate, String seatId) {
        String key = generateKey(concertDate, seatId);
        seatStore.remove(key);
    }
    
    public boolean isSeatAvailable(String seatId) {
        // 모든 좌석을 검사하여 해당 좌석이 사용 가능한지 확인
        for (Seat seat : seatStore.values()) {
            if (seat.getSeatId().equals(seatId) && seat.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void holdSeat(String seatId, String reservationId) {
        // 좌석을 임시 배정 상태로 변경
        for (Seat seat : seatStore.values()) {
            if (seat.getSeatId().equals(seatId)) {
                seat.holdSeat(reservationId, 5); // 5분 임시 배정
                break;
            }
        }
    }

    public void sellSeat(String seatId) {
        // 좌석을 판매 완료 상태로 변경
        for (Seat seat : seatStore.values()) {
            if (seat.getSeatId().equals(seatId)) {
                seat.sellSeat();
                break;
            }
        }
    }

    public void releaseSeat(String seatId) {
        // 좌석을 사용 가능 상태로 변경
        for (Seat seat : seatStore.values()) {
            if (seat.getSeatId().equals(seatId)) {
                seat.releaseSeat();
                break;
            }
        }
    }

    private String generateKey(String concertDate, String seatId) {
        return concertDate + "#" + seatId;
    }
}
