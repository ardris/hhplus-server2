package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.Performance;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 공연 정보 저장소
 */
@Repository
public class PerformanceRepository {
    private final Map<String, Performance> performances = new ConcurrentHashMap<>();

    public Performance save(Performance performance) {
        performances.put(performance.getPerformanceId(), performance);
        return performance;
    }

    public Optional<Performance> findById(String performanceId) {
        return Optional.ofNullable(performances.get(performanceId));
    }

    public List<Performance> findAll() {
        return new ArrayList<>(performances.values());
    }

    public List<Performance> findByConcertId(String concertId) {
        List<Performance> performanceList = new ArrayList<>();
        for (Performance currentPerformance : performances.values()) {
            if (currentPerformance.getConcertId().equals(concertId)) {
                performanceList.add(currentPerformance);
            }
        }
        return performanceList;
    }

    public List<Performance> findByVenueId(String venueId) {
        List<Performance> performanceList = new ArrayList<>();
        for (Performance currentPerformance : performances.values()) {
            if (currentPerformance.getVenueId().equals(venueId)) {
                performanceList.add(currentPerformance);
            }
        }
        return performanceList;
    }

    public List<Performance> findByDate(LocalDate date) {
        List<Performance> performanceList = new ArrayList<>();
        for (Performance currentPerformance : performances.values()) {
            if (currentPerformance.getPerformanceDate().equals(date)) {
                performanceList.add(currentPerformance);
            }
        }
        return performanceList;
    }

    public List<Performance> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Performance> performanceList = new ArrayList<>();
        for (Performance currentPerformance : performances.values()) {
            LocalDate performanceDate = currentPerformance.getPerformanceDate();
            if (!performanceDate.isBefore(startDate) && !performanceDate.isAfter(endDate)) {
                performanceList.add(currentPerformance);
            }
        }
        return performanceList;
    }

    public List<String> findVenueIdsWithPerformancesInDateRange(LocalDate startDate, LocalDate endDate) {
        List<String> venueIdList = new ArrayList<>();
        for (Performance currentPerformance : performances.values()) {
            LocalDate performanceDate = currentPerformance.getPerformanceDate();
            if (!performanceDate.isBefore(startDate) && !performanceDate.isAfter(endDate)) {
                String venueId = currentPerformance.getVenueId();
                // 중복 제거를 위한 체크
                if (!venueIdList.contains(venueId)) {
                    venueIdList.add(venueId);
                }
            }
        }
        return venueIdList;
    }

    public List<Performance> findActivePerformances() {
        List<Performance> performanceList = new ArrayList<>();
        for (Performance currentPerformance : performances.values()) {
            if (currentPerformance.isActive()) {
                performanceList.add(currentPerformance);
            }
        }
        return performanceList;
    }

    public void deleteById(String performanceId) {
        performances.remove(performanceId);
    }

    public long count() {
        return performances.size();
    }

    /**
     * 초기 공연 데이터 설정
     */
    public void initializePerformances() {
        // BTS 콘서트 - 서울 올림픽 경기장
        Performance btsSeoul = new Performance(
            "PERF-001",
            "CONCERT-001", // BTS 콘서트
            "VENUE-001",   // 서울 올림픽 경기장
            LocalDate.of(2025, 7, 19),
            LocalTime.of(19, 0),
            new BigDecimal("150000"),
            true
        );
        save(btsSeoul);

        // BTS 콘서트 - 부산 아시아드 주경기장
        Performance btsBusan = new Performance(
            "PERF-002",
            "CONCERT-001", // BTS 콘서트
            "VENUE-002",   // 부산 아시아드 주경기장
            LocalDate.of(2025, 7, 20),
            LocalTime.of(19, 0),
            new BigDecimal("150000"),
            true
        );
        save(btsBusan);

        // 아이유 콘서트 - 대구 실내체육관
        Performance iuDaegu = new Performance(
            "PERF-003",
            "CONCERT-002", // 아이유 콘서트
            "VENUE-003",   // 대구 실내체육관
            LocalDate.of(2025, 7, 19),
            LocalTime.of(20, 0),
            new BigDecimal("120000"),
            true
        );
        save(iuDaegu);
    }
}