package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.Venue;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 공연장 정보 저장소
 */
@Repository
public class VenueRepository {
    private final Map<String, Venue> venues = new ConcurrentHashMap<>();

    public Venue save(Venue venue) {
        venues.put(venue.getVenueId(), venue);
        return venue;
    }

    public Optional<Venue> findById(String venueId) {
        return Optional.ofNullable(venues.get(venueId));
    }

    public List<Venue> findAll() {
        return new ArrayList<>(venues.values());
    }

    public List<Venue> findByCity(String city) {
        List<Venue> result = new ArrayList<>();
        for (Venue venue : venues.values()) {
            if (venue.getVenueCity().equals(city)) {
                result.add(venue);
            }
        }
        return result;
    }

    public List<Venue> findActiveVenues() {
        List<Venue> result = new ArrayList<>();
        for (Venue venue : venues.values()) {
            if (venue.isActive()) {
                result.add(venue);
            }
        }
        return result;
    }

    public void deleteById(String venueId) {
        venues.remove(venueId);
    }

    public long count() {
        return venues.size();
    }

    /**
     * 초기 공연장 데이터 설정
     */
    public void initializeVenues() {
        // 서울 올림픽 경기장
        Venue seoulOlympic = new Venue(
            "VENUE-001",
            "올림픽 경기장",
            "서울특별시 송파구 올림픽로 424",
            "서울",
            10000,
            "올림픽 경기장",
            true
        );
        save(seoulOlympic);

        // 부산 아시아드 주경기장
        Venue busanAsiad = new Venue(
            "VENUE-002",
            "아시아드 주경기장",
            "부산광역시 해운대구 수영강로 344",
            "부산",
            5000,
            "아시아드 주경기장",
            true
        );
        save(busanAsiad);

        // 대구 실내체육관
        Venue daeguGym = new Venue(
            "VENUE-003",
            "실내체육관",
            "대구광역시 수성구 동대구로 465",
            "대구",
            3000,
            "실내체육관",
            true
        );
        save(daeguGym);
    }
}
