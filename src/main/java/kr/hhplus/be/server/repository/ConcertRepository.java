package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.Concert;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 콘서트 별 정보를 관리하는 인메모리 저장소
 */
public class ConcertRepository {
    
    private final Map<String, Concert> concertStore = new ConcurrentHashMap<>();
    
    // 콘서트 저장
    public Concert save(Concert concert) {
        concertStore.put(concert.getConcertId(), concert);
        return concert;
    }
    
    // 콘서트 ID로 콘서트 조회
    public Optional<Concert> findByConcertId(String concertId) {
        return Optional.ofNullable(concertStore.get(concertId));
    }
    
    // 예약 가능한 콘서트 목록 조회
    public List<Concert> findAvailableConcerts() {
        List<Concert> availableConcerts = new ArrayList<>();
        for (Concert concert : concertStore.values()) {
            if (concert.isActive() && concert.isConcertDateAvailable()) {
                availableConcerts.add(concert);
            }
        }
        return availableConcerts;
    }
    
    // 예약 가능한 콘서트 날짜 목록 조회
    public List<String> getAvailableConcertDates() {
        Set<String> dateSet = new HashSet<>();
        for (Concert concert : findAvailableConcerts()) {
            dateSet.add(concert.getConcertDate().toString());
        }
        List<String> dates = new ArrayList<>(dateSet);
        Collections.sort(dates);
        return dates;
    }
    
    // 콘서트 날짜로 콘서트 조회
    public Optional<Concert> findByConcertDate(LocalDate concertDate) {
        for (Concert concert : concertStore.values()) {
            if (concert.getConcertDate().equals(concertDate) && concert.isActive()) {
                return Optional.of(concert);
            }
        }
        return Optional.empty();
    }
    
    // 콘서트 날짜와 제목으로 콘서트 조회
    public Concert findByDateAndTitle(String concertDate, String concertTitle) {
        for (Concert concert : concertStore.values()) {
            if (concert.getDate().equals(concertDate) && 
                concert.getTitle().equals(concertTitle) && 
                concert.isActive()) {
                return concert;
            }
        }
        return null;
    }
    
    // 모든 콘서트 조회
    public List<Concert> findAll() {
        return new ArrayList<>(concertStore.values());
    }
    
    // 콘서트 정보 업데이트
    public void update(Concert concert) {
        concertStore.put(concert.getConcertId(), concert);
    }
    
    // 콘서트 존재 여부 확인
    public boolean existsByConcertId(String concertId) {
        return concertStore.containsKey(concertId);
    }
    
    public void deleteByConcertId(String concertId) {
        concertStore.remove(concertId);
    }
}
