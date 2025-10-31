package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.application.MakeReservationUseCase;
import kr.hhplus.be.server.application.ProcessPaymentUseCase;
import kr.hhplus.be.server.application.MakeReservationUseCase.MakeReservationCommand;
import kr.hhplus.be.server.application.ProcessPaymentUseCase.ProcessPaymentCommand;
import kr.hhplus.be.server.application.ProcessPaymentUseCase.PaymentResult;
import kr.hhplus.be.server.domain.port.ReservationRepositoryPort;
import kr.hhplus.be.server.domain.port.PaymentRepositoryPort;
import kr.hhplus.be.server.domain.port.SeatPort;
import kr.hhplus.be.server.domain.port.UserPort;
import kr.hhplus.be.server.infrastructure.adapter.JpaReservationAdapter;
import kr.hhplus.be.server.infrastructure.adapter.JpaPaymentAdapter;
import kr.hhplus.be.server.infrastructure.adapter.JpaSeatAdapter;
import kr.hhplus.be.server.infrastructure.adapter.JpaUserAdapter;
import kr.hhplus.be.server.infrastructure.repository.*;
import kr.hhplus.be.server.infrastructure.entity.*;
import kr.hhplus.be.server.dto.request.BalanceChargeRequest;
import kr.hhplus.be.server.dto.request.ReservationRequest;
import kr.hhplus.be.server.dto.response.BalanceChargeResponse;
import kr.hhplus.be.server.dto.response.PagedResponse;
import kr.hhplus.be.server.dto.response.ConcertResponse;
import kr.hhplus.be.server.dto.response.VenueResponse;
import kr.hhplus.be.server.dto.response.DatePerformanceResponse;
import kr.hhplus.be.server.dto.response.ConcertSeatResponse;
import kr.hhplus.be.server.dto.response.BalanceResponse;
import kr.hhplus.be.server.dto.response.PaymentResponse;
import kr.hhplus.be.server.dto.response.PerformanceInfo;
import kr.hhplus.be.server.dto.response.ReservationResponse;
import kr.hhplus.be.server.dto.response.TokenResponse;
import kr.hhplus.be.server.dto.response.VenueInfo;
import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.domain.entity.Reservation;
import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.service.QueueService;
import kr.hhplus.be.server.service.ConcertService;
import kr.hhplus.be.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 콘서트 예약 서비스 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1")
public class ReservationController {

    private final MakeReservationUseCase makeReservationUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final QueueService queueService;
    private final ConcertService concertService;
    private final UserService userService;
    private final JpaConcertRepository jpaConcertRepository;
    private final JpaUserRepository jpaUserRepository;
    private final JpaVenueRepository jpaVenueRepository;
    private final JpaPerformanceRepository jpaPerformanceRepository;
    private final JpaReservationRepository jpaReservationRepository;
    private final JpaPaymentRepository jpaPaymentRepository;
    private final JpaSeatRepository jpaSeatRepository;
    private final JpaSeatReservationStatusRepository jpaSeatReservationStatusRepository;
    private final JpaQueueTokenRepository jpaQueueTokenRepository;
    private final JpaPerformanceSeatPricingRepository jpaPerformanceSeatPricingRepository;

    @Autowired
    public ReservationController(
            // JPA Repository들 주입
            JpaConcertRepository jpaConcertRepository,
            JpaUserRepository jpaUserRepository,
            JpaVenueRepository jpaVenueRepository,
            JpaPerformanceRepository jpaPerformanceRepository,
            JpaPerformanceSeatPricingRepository jpaPerformanceSeatPricingRepository,
            JpaReservationRepository jpaReservationRepository,
            JpaPaymentRepository jpaPaymentRepository,
            JpaSeatRepository jpaSeatRepository,
            JpaSeatReservationStatusRepository jpaSeatReservationStatusRepository,
            JpaQueueTokenRepository jpaQueueTokenRepository,
            // Service들 주입
            QueueService queueService,
            ConcertService concertService,
            UserService userService) {
        
        // JPA Repository들 초기화
        this.jpaConcertRepository = jpaConcertRepository;
        this.jpaUserRepository = jpaUserRepository;
        this.jpaVenueRepository = jpaVenueRepository;
        this.jpaPerformanceRepository = jpaPerformanceRepository;
        this.jpaPerformanceSeatPricingRepository = jpaPerformanceSeatPricingRepository;
        this.jpaReservationRepository = jpaReservationRepository;
        this.jpaPaymentRepository = jpaPaymentRepository;
        this.jpaSeatRepository = jpaSeatRepository;
        this.jpaSeatReservationStatusRepository = jpaSeatReservationStatusRepository;
        this.jpaQueueTokenRepository = jpaQueueTokenRepository;

        // Service들 초기화
        this.queueService = queueService;
        this.concertService = concertService;
        this.userService = userService;

        // 클린아키텍처 포트와 어댑터 초기화
        ReservationRepositoryPort reservationRepositoryPort = new JpaReservationAdapter(jpaReservationRepository);
        PaymentRepositoryPort paymentRepositoryPort = new JpaPaymentAdapter(jpaPaymentRepository);
        SeatPort seatPort = new JpaSeatAdapter(jpaSeatRepository, jpaSeatReservationStatusRepository);
        UserPort userPort = new JpaUserAdapter(jpaUserRepository);

        // 클린아키텍처 Use Case 초기화
        this.makeReservationUseCase = new MakeReservationUseCase(reservationRepositoryPort, seatPort, userPort);
        this.processPaymentUseCase = new ProcessPaymentUseCase(paymentRepositoryPort, reservationRepositoryPort, userService);
    }

    /**
     * 대기열 토큰 발급
     */
    @PostMapping("/queue/token")
    public ResponseEntity<TokenResponse> issueQueueToken(@RequestParam String userId) {
        try {
            // 입력 검증
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null); // 400: 유효하지 않은 아이디입니다.
            }

            String tokenId = queueService.issueToken(userId);
            QueueService.QueueStatus queueStatus = queueService.getQueueStatus(tokenId);

            TokenResponse response = new TokenResponse(tokenId, queueStatus);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500: 서버에 문제가 있습니다.
        }
    }

    /**
     * 예약 가능한 콘서트 목록 조회 (공연장 정보 포함)
     */
    @GetMapping("/concerts")
    public ResponseEntity<PagedResponse<ConcertResponse>> getAvailableConcerts(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        queueService.validateToken(tokenId);

        // 페이지네이션 적용하여 콘서트 조회
        List<kr.hhplus.be.server.infrastructure.entity.ConcertEntity> allConcertEntities = jpaConcertRepository.findAll();
        
        // 날짜 필터링
        List<kr.hhplus.be.server.infrastructure.entity.ConcertEntity> filteredEntities = new ArrayList<>();
        for (kr.hhplus.be.server.infrastructure.entity.ConcertEntity entity : allConcertEntities) {
            if (startDate != null && endDate != null) {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                if (entity.getConcertPeriodStart().isAfter(start.minusDays(1)) && 
                    entity.getConcertPeriodEnd().isBefore(end.plusDays(1))) {
                    filteredEntities.add(entity);
                }
            } else {
                filteredEntities.add(entity);
            }
        }
        
        // 페이지네이션 적용
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, filteredEntities.size());
        List<kr.hhplus.be.server.infrastructure.entity.ConcertEntity> paginatedEntities = 
            filteredEntities.subList(startIndex, endIndex);
        
        List<Concert> concerts = new ArrayList<>();
        for (kr.hhplus.be.server.infrastructure.entity.ConcertEntity entity : paginatedEntities) {
            concerts.add(entity.toDomain());
        }
        
        long totalCount = filteredEntities.size();

        List<ConcertResponse> concertResponses = new ArrayList<>();
        for (Concert currentConcert : concerts) {
            // 해당 콘서트의 첫 번째 공연 정보 조회
            List<kr.hhplus.be.server.infrastructure.entity.PerformanceEntity> performanceEntities = 
                jpaPerformanceRepository.findByConcertId(currentConcert.getConcertId());
            
            String venueName = "알 수 없는 공연장";
            String venueCity = "알 수 없는 도시";
            BigDecimal ticketPrice = BigDecimal.ZERO;
            int availableSeats = 0;
            
            if (!performanceEntities.isEmpty()) {
                kr.hhplus.be.server.infrastructure.entity.PerformanceEntity firstPerformance = performanceEntities.get(0);
                
                // 좌석 가격 조회 (DB 스키마에 맞게 수정)
                List<PerformanceSeatPricingEntity> pricingEntities = 
                    jpaPerformanceSeatPricingRepository.findByPerformanceId(firstPerformance.getPerformanceId());
                if (!pricingEntities.isEmpty()) {
                    ticketPrice = pricingEntities.get(0).getPrice(); // 첫 번째 등급 가격 사용
                }
                
                // 공연장 정보 조회
                Optional<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueOptional = 
                    jpaVenueRepository.findById(firstPerformance.getVenueId());
                if (venueOptional.isPresent()) {
                    kr.hhplus.be.server.infrastructure.entity.VenueEntity venueEntity = venueOptional.get();
                    venueName = venueEntity.getVenueName();
                    venueCity = venueEntity.getVenueCity();
                    // 실제 예약된 좌석 수 조회하여 차감
                    int totalCapacity = venueEntity.getVenueCapacity();
                    int reservedSeats = getReservedSeatCountForPerformance(firstPerformance.getPerformanceId());
                    availableSeats = Math.max(0, totalCapacity - reservedSeats);
                }
            }

            ConcertResponse concertResponse = new ConcertResponse(
                    currentConcert.getConcertId(),
                    currentConcert.getConcertName(),
                    currentConcert.getConcertPeriodStart(),
                    venueName,
                    venueCity,
                    ticketPrice,
                    availableSeats,
                    currentConcert.isActive());
            concertResponses.add(concertResponse);
        }

        PagedResponse<ConcertResponse> response = new PagedResponse<ConcertResponse>(
                concertResponses, page, size, totalCount);

        return ResponseEntity.ok(response);
    }

    /**
     * 2-1. 예약 가능한 콘서트 날짜 조회 API (기존 호환성 유지)
     */
    @GetMapping("/concerts/dates")
    // 예약 가능한 콘서트 날짜 목록 조회
    public ResponseEntity<List<String>> getAvailableConcertDates(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        queueService.validateToken(tokenId);
        List<String> dates = concertService.getAvailableConcertDates();
        return ResponseEntity.ok(dates);
    }

    /**
     * 특정 날짜의 모든 공연 조회
     */
    @GetMapping("/performances/date/{date}")
    // 특정 날짜의 모든 공연 조회
    public ResponseEntity<DatePerformanceResponse> getPerformancesByDate(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @PathVariable String date) {

        // 대기열 토큰 검증
        queueService.validateToken(tokenId);

        // 입력받은 날짜 문자열을 LocalDate 객체로 변환
        LocalDate searchDate = LocalDate.parse(date);

        // 해당 날짜의 모든 공연 목록 조회
        List<kr.hhplus.be.server.infrastructure.entity.PerformanceEntity> performanceEntities = jpaPerformanceRepository.findByPerformanceDate(searchDate);
        List<kr.hhplus.be.server.model.Performance> performanceList = new ArrayList<>();
        for (kr.hhplus.be.server.infrastructure.entity.PerformanceEntity entity : performanceEntities) {
            performanceList.add(entity.toDomain());
        }

        // 공연 상세 정보를 담을 리스트 생성
        List<DatePerformanceResponse.PerformanceInfo> performanceInfoList = new ArrayList<>();

        // 각 공연에 대해 상세 정보 조회 및 응답 객체 생성
        for (kr.hhplus.be.server.model.Performance currentPerformance : performanceList) {
            // 공연장 정보 조회
            Optional<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueOptional = jpaVenueRepository
                    .findById(currentPerformance.getVenueId());
            String venueName = "알 수 없는 공연장";
            String venueCity = "알 수 없는 도시";

            if (venueOptional.isPresent()) {
                kr.hhplus.be.server.infrastructure.entity.VenueEntity venueEntity = venueOptional.get();
                venueName = venueEntity.getVenueName();
                venueCity = venueEntity.getVenueCity();
            }

            // 콘서트 정보 조회
            Optional<kr.hhplus.be.server.infrastructure.entity.ConcertEntity> concertOptional = jpaConcertRepository.findById(currentPerformance.getConcertId());
            String concertName = "알 수 없는 콘서트";

            if (concertOptional.isPresent()) {
                kr.hhplus.be.server.infrastructure.entity.ConcertEntity concertEntity = concertOptional.get();
                concertName = concertEntity.getConcertName();
            }

            // 예약 가능한 좌석 수 조회 (현재 사용하지 않음)
            // int availableSeatCount = getAvailableSeatCount(currentPerformance.getPerformanceId());

            // 공연 상세 정보 객체 생성
            DatePerformanceResponse.PerformanceInfo performanceDetailInfo = new DatePerformanceResponse.PerformanceInfo(
                    currentPerformance.getPerformanceId(),
                    concertName,
                    venueName,
                    venueCity,
                    currentPerformance.getPerformanceTime().toString(),
                    currentPerformance.getTicketPrice());

            performanceInfoList.add(performanceDetailInfo);
        }

        // 최종 응답 객체 생성
        DatePerformanceResponse finalResponse = new DatePerformanceResponse(date, performanceInfoList);

        return ResponseEntity.ok(finalResponse);
    }

    /**
     * 공연장 목록 조회
     */
    @GetMapping("/venues")
    // 공연장 목록 조회
    public ResponseEntity<Map<String, Object>> getVenues(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "true") boolean activeOnly) {

        // 대기열 토큰 검증
        queueService.validateToken(tokenId);

        // 공연장 목록 조회
        List<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueEntities;
        if (activeOnly) {
            venueEntities = jpaVenueRepository.findActiveVenues();
        } else {
            venueEntities = jpaVenueRepository.findAll();
        }
        
        List<kr.hhplus.be.server.model.Venue> venueList = new ArrayList<>();
        for (kr.hhplus.be.server.infrastructure.entity.VenueEntity entity : venueEntities) {
            venueList.add(entity.toDomain());
        }

        // 날짜 범위가 지정된 경우, 해당 기간에 공연이 있는 공연장만 필터링
        if (startDate != null && endDate != null) {
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            LocalDate parsedEndDate = LocalDate.parse(endDate);

            // 해당 기간에 공연이 있는 공연장 ID 목록 조회
            List<String> venueIdListWithPerformances = jpaPerformanceRepository
                    .findVenueIdsWithPerformancesInDateRange(parsedStartDate, parsedEndDate);

            // 공연이 있는 공연장만 필터링
            List<kr.hhplus.be.server.model.Venue> filteredVenueList = new ArrayList<>();
            for (kr.hhplus.be.server.model.Venue currentVenue : venueList) {
                String currentVenueId = currentVenue.getVenueId();
                if (venueIdListWithPerformances.contains(currentVenueId)) {
                    filteredVenueList.add(currentVenue);
                }
            }
            venueList = filteredVenueList;
        }

        // 공연장 응답 리스트 생성
        List<VenueResponse> venueResponseList = new ArrayList<>();
        for (kr.hhplus.be.server.model.Venue currentVenue : venueList) {
            String venueId = currentVenue.getVenueId();
            String venueName = currentVenue.getVenueName();
            String venueAddress = currentVenue.getVenueAddress();
            String venueCity = currentVenue.getVenueCity();
            int venueCapacity = currentVenue.getVenueCapacity();
            String venueDescription = currentVenue.getVenueDescription();
            boolean isActive = currentVenue.isActive();

            VenueResponse venueResponse = new VenueResponse(
                    venueId,
                    venueName,
                    venueAddress,
                    venueCity,
                    venueCapacity,
                    venueDescription,
                    isActive);
            venueResponseList.add(venueResponse);
        }

        // 응답 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("venues", venueResponseList);
        response.put("totalCount", venueResponseList.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 공연장 상세 정보 조회
     */
    @GetMapping("/venues/{venueId}")
    // 공연장 상세 정보 조회
    public ResponseEntity<VenueResponse> getVenueById(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @PathVariable String venueId) {

        // 대기열 토큰 검증
        queueService.validateToken(tokenId);

        // 공연장 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueOptional = jpaVenueRepository.findById(venueId);
        if (venueOptional.isEmpty()) {
            throw new IllegalArgumentException("해당 공연장을 찾을 수 없습니다.");
        }

        kr.hhplus.be.server.infrastructure.entity.VenueEntity venueEntity = venueOptional.get();

        // 공연장 정보 추출
        String venueIdFromEntity = venueEntity.getVenueId();
        String venueName = venueEntity.getVenueName();
        String venueAddress = venueEntity.getVenueAddress();
        String venueCity = venueEntity.getVenueCity();
        int venueCapacity = venueEntity.getVenueCapacity();
        String venueDescription = venueEntity.getVenueDescription();
        boolean isActive = venueEntity.isActive();

        // 응답 객체 생성
        VenueResponse venueResponse = new VenueResponse(
                venueIdFromEntity,
                venueName,
                venueAddress,
                venueCity,
                venueCapacity,
                venueDescription,
                isActive);

        return ResponseEntity.ok(venueResponse);
    }

    /**
     * 2-5. 도시별 공연장 조회 API (전통적인 Java 스타일 구현)
     */
    @GetMapping("/venues/city/{city}")
    // 특정 도시의 공연장 목록 조회
    public ResponseEntity<Map<String, Object>> getVenuesByCity(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @PathVariable String city) {

        // 대기열 토큰 검증
        queueService.validateToken(tokenId);

        // 도시별 공연장 조회
        List<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueEntities = jpaVenueRepository.findByCity(city);
        List<kr.hhplus.be.server.model.Venue> venueList = new ArrayList<>();
        for (kr.hhplus.be.server.infrastructure.entity.VenueEntity entity : venueEntities) {
            venueList.add(entity.toDomain());
        }

        if (venueList.isEmpty()) {
            throw new IllegalArgumentException("해당 도시에 공연장이 없습니다.");
        }

        // 공연장 응답 리스트 생성
        List<VenueResponse> venueResponseList = new ArrayList<>();
        for (kr.hhplus.be.server.model.Venue currentVenue : venueList) {
            String venueId = currentVenue.getVenueId();
            String venueName = currentVenue.getVenueName();
            String venueAddress = currentVenue.getVenueAddress();
            String venueCity = currentVenue.getVenueCity();
            int venueCapacity = currentVenue.getVenueCapacity();
            String venueDescription = currentVenue.getVenueDescription();
            boolean isActive = currentVenue.isActive();

            VenueResponse venueResponse = new VenueResponse(
                    venueId,
                    venueName,
                    venueAddress,
                    venueCity,
                    venueCapacity,
                    venueDescription,
                    isActive);
            venueResponseList.add(venueResponse);
        }

        // 응답 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("venues", venueResponseList);
        response.put("totalCount", venueResponseList.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 3. 특정 콘서트의 좌석 상세 정보 조회 API (개선된 버전)
     */
    @GetMapping("/concerts/seats/details")
    // 특정 콘서트의 좌석 상세 정보 조회 (콘서트 정보 + 좌석 상태 포함)
    public ResponseEntity<ConcertSeatResponse> getConcertSeatDetails(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestParam String concertDate,
            @RequestParam String concertTitle) {
        queueService.validateToken(tokenId);
        // kr.hhplus.be.server.model.ConcertSeatResponse modelResponse = concertService.getConcertSeatDetails(concertDate, concertTitle);
        // TODO: model.ConcertSeatResponse를 dto.response.ConcertSeatResponse로 변환
        ConcertSeatResponse response = new ConcertSeatResponse(); // 임시
        return ResponseEntity.ok(response);
    }

    /**
     * 3-1. 특정 날짜의 예약 가능한 좌석 조회 API (기존 호환성 유지)
     */
    @GetMapping("/concerts/seats")
    // 특정 날짜의 예약 가능한 좌석 목록 조회
    public ResponseEntity<List<String>> getAvailableSeats(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestParam String concertDate) {
        queueService.validateToken(tokenId);
        List<String> seats = concertService.getAvailableSeatsByDate(concertDate);
        return ResponseEntity.ok(seats);
    }

    /**
     * 좌석 예약 요청
     */
    @PostMapping("/reservations")
    // 좌석 예약 요청 (5분 임시 배정)
    public ResponseEntity<ReservationResponse> makeReservation(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestBody ReservationRequest request) {

        // 입력 검증
        if (request.getConcertDate() == null || request.getConcertDate().trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 날짜는 필수입니다.");
        }
        if (request.getConcertTitle() == null || request.getConcertTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("콘서트 제목은 필수입니다.");
        }
        if (request.getSeatId() == null || request.getSeatId().trim().isEmpty()) {
            throw new IllegalArgumentException("좌석 ID는 필수입니다.");
        }

        // 토큰에서 사용자 ID 조회
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // Performance 정보 조회하여 가격 확인
        kr.hhplus.be.server.model.Performance currentPerformance = findPerformanceByDateAndTitle(
                request.getConcertDate(), request.getConcertTitle());
        if (currentPerformance == null) {
            throw new IllegalArgumentException("공연을 찾을 수 없습니다.");
        }

        // 클린아키텍처 Use Case 호출
        MakeReservationCommand reservationCommand = new MakeReservationCommand(
                userId,
                currentPerformance.getPerformanceId(), // performanceId 사용
                request.getSeatId(),
                currentPerformance.getTicketPrice());

        kr.hhplus.be.server.domain.entity.Reservation createdReservation = makeReservationUseCase
                .execute(reservationCommand);

        // 공연장 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueOptional = jpaVenueRepository
                .findById(currentPerformance.getVenueId());
        if (venueOptional.isEmpty()) {
            throw new IllegalArgumentException("공연장 정보를 찾을 수 없습니다.");
        }
        kr.hhplus.be.server.infrastructure.entity.VenueEntity venueEntity = venueOptional.get();
        // kr.hhplus.be.server.model.Venue venueInformation = venueEntity.toDomain();

        // 응답 생성
        ReservationResponse.ConcertInfo concertInformation = new ReservationResponse.ConcertInfo(
                currentPerformance.getPerformanceDate().toString(),
                getConcertNameByPerformance(currentPerformance),
                currentPerformance.getTicketPrice());

        // 공연장 정보 생성
        VenueInfo venueInfo = new VenueInfo(
                venueEntity.getVenueId(),
                venueEntity.getVenueName(),
                venueEntity.getVenueCity(),
                venueEntity.getVenueAddress(),
                venueEntity.getVenueCapacity(),
                new ArrayList<>() // 공연장 정보만 필요하므로 빈 리스트
        );

        ReservationResponse.SeatInfo seatInformation = new ReservationResponse.SeatInfo(
                request.getSeatId(),
                Integer.parseInt(request.getSeatId()));

        ReservationResponse reservationResponse = new ReservationResponse(
                createdReservation.getReservationId(),
                concertInformation,
                venueInfo,
                seatInformation,
                createdReservation.getHoldExpiresAt().toString(),
                createdReservation.getStatus().toString());

        return ResponseEntity.ok(reservationResponse);
    }

    /**
     * 5. 잔액 충전 API - 개선된 버전
     */
    @PostMapping("/balance/charge")
    // 사용자 잔액 충전 (거래 내역 포함)
    public ResponseEntity<BalanceChargeResponse> chargeBalance(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestBody BalanceChargeRequest request) {

        // 입력 검증
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        // 잔액 충전 (거래 내역 포함)
        Transaction chargeTransaction = userService.chargeBalanceWithTransaction(tokenId, request.getAmount());

        // 토큰에서 사용자 ID 조회
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // 사용자 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.UserEntity> userOptional = jpaUserRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        kr.hhplus.be.server.infrastructure.entity.UserEntity userEntity = userOptional.get();
        BigDecimal previousBalanceAmount = userEntity.getBalance().subtract(request.getAmount());

        // 응답 생성
        BalanceChargeResponse chargeResponse = new BalanceChargeResponse(
                userId,
                previousBalanceAmount,
                request.getAmount(),
                userEntity.getBalance(),
                chargeTransaction.getTransactionId(),
                chargeTransaction.getCreatedAt().toString());

        return ResponseEntity.ok(chargeResponse);
    }

    /**
     * 6. 잔액 조회 API - 개선된 버전
     */
    @GetMapping("/balance")
    // 사용자 잔액 조회 (거래 내역 포함)
    public ResponseEntity<BalanceResponse> getBalance(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        // 토큰 검증
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // 사용자 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.UserEntity> userOptional = jpaUserRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        kr.hhplus.be.server.infrastructure.entity.UserEntity userEntity = userOptional.get();

        // 최근 거래 내역 조회 (최대 10개)
        List<Transaction> recentTransactionList = userService.getUserTransactions(tokenId, 10);

        // 거래 내역을 응답 형식으로 변환
        List<BalanceResponse.TransactionInfo> transactionInfoList = new ArrayList<>();
        for (Transaction currentTransaction : recentTransactionList) {
            transactionInfoList.add(new BalanceResponse.TransactionInfo(
                    currentTransaction.getTransactionId(),
                    currentTransaction.getType().toString(),
                    currentTransaction.getAmount(),
                    currentTransaction.getBalanceAfter(),
                    currentTransaction.getCreatedAt().toString()));
        }

        // 응답 생성
        BalanceResponse balanceResponse = new BalanceResponse(
                userId,
                userEntity.getBalance(),
                userEntity.getUpdatedAt().toString(),
                transactionInfoList);

        return ResponseEntity.ok(balanceResponse);
    }

    /**
     * 결제 처리
     */
    @PostMapping("/payments/{reservationId}")
    // 예약 결제 처리
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @PathVariable String reservationId) {

        // 토큰 검증
        queueService.validateToken(tokenId);

        // 클린아키텍처 Use Case 호출
        ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(reservationId);
        PaymentResult paymentResult = processPaymentUseCase.execute(paymentCommand);

        if (!paymentResult.isSuccess()) {
            return ResponseEntity.badRequest().body(null);
        }

        // 예약 정보 조회하여 공연장 정보 포함
        Optional<kr.hhplus.be.server.domain.entity.Reservation> reservationOptional = jpaReservationRepository
                .findById(reservationId).map(ReservationEntity::toDomain);
        if (reservationOptional.isEmpty()) {
            throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
        }

        kr.hhplus.be.server.domain.entity.Reservation reservationInformation = reservationOptional.get();

        // Performance 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.PerformanceEntity> performanceOptional = jpaPerformanceRepository
                .findById(reservationInformation.getPerformanceId());
        if (performanceOptional.isEmpty()) {
            throw new IllegalArgumentException("공연 정보를 찾을 수 없습니다.");
        }

        kr.hhplus.be.server.infrastructure.entity.PerformanceEntity performanceEntity = performanceOptional.get();

        // 공연장 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueOptional = jpaVenueRepository
                .findById(performanceEntity.getVenueId());
        if (venueOptional.isEmpty()) {
            throw new IllegalArgumentException("공연장 정보를 찾을 수 없습니다.");
        }

        kr.hhplus.be.server.infrastructure.entity.VenueEntity venueEntity = venueOptional.get();

        // 콘서트 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.ConcertEntity> concertOptional = jpaConcertRepository.findById(performanceEntity.getConcertId());
        if (concertOptional.isEmpty()) {
            throw new IllegalArgumentException("콘서트 정보를 찾을 수 없습니다.");
        }

        kr.hhplus.be.server.infrastructure.entity.ConcertEntity concertEntity = concertOptional.get();

        // 좌석 가격 조회
        BigDecimal ticketPrice = BigDecimal.ZERO;
        Optional<PerformanceSeatPricingEntity> pricingOptional = 
            jpaPerformanceSeatPricingRepository.findByPerformanceIdAndSeatGrade(
                performanceEntity.getPerformanceId(), 
                reservationInformation.getSeatGrade());
        if (pricingOptional.isPresent()) {
            ticketPrice = pricingOptional.get().getPrice();
        }
        
        // 응답 객체 생성
        PaymentResponse.ReservationInfo reservationInfo = new PaymentResponse.ReservationInfo(
                reservationId,
                performanceEntity.getPerformanceDate().toString(),
                concertEntity.getConcertName(),
                venueEntity.getVenueName(),
                venueEntity.getVenueCity(),
                Integer.parseInt(reservationInformation.getSeatId()));

        PaymentResponse.PaymentInfo paymentInfo = new PaymentResponse.PaymentInfo(
                ticketPrice,
                "COMPLETED",
                java.time.LocalDateTime.now().toString());

        PaymentResponse.UserInfo userInfo = new PaymentResponse.UserInfo(
                reservationInformation.getUserId(),
                BigDecimal.ZERO, // TODO: 실제 잔액 조회 필요
                BigDecimal.ZERO // TODO: 실제 잔액 조회 필요
        );

        // 성공 시 응답 생성
        PaymentResponse paymentResponse = new PaymentResponse(
                paymentResult.getPaymentId(),
                reservationInfo,
                paymentInfo,
                userInfo);

        return ResponseEntity.ok(paymentResponse);
    }

    /**
     * 8. 대기열 상태 조회 API
     */
    @GetMapping("/queue/status")
    public ResponseEntity<QueueService.QueueStatus> getQueueStatus(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        try {
            QueueService.QueueStatus status = queueService.getQueueStatus(tokenId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500: 서버에 문제가 있습니다.
        }
    }

    /**
     * 9. 예약 조회 API
     */
    @GetMapping("/reservations/{reservationId}")
    // 특정 예약 정보 조회
    public ResponseEntity<Reservation> getReservation(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @PathVariable String reservationId) {
        // 토큰 검증
        queueService.validateToken(tokenId);
        
        // 예약 조회
        Optional<Reservation> reservationOptional = jpaReservationRepository.findById(reservationId)
                .map(ReservationEntity::toDomain);
        if (reservationOptional.isEmpty()) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
        
        return ResponseEntity.ok(reservationOptional.get());
    }

    /**
     * 10. 사용자 예약 목록 조회 API (공연장 정보 포함)
     */
    @GetMapping("/reservations")
    // 사용자의 모든 예약 목록 조회 (공연장 정보 포함)
    public ResponseEntity<List<ReservationResponse>> getUserReservations(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        // 토큰 검증
        queueService.validateToken(tokenId);

        // 사용자 ID 조회
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // 사용자의 예약 목록 조회
        List<ReservationEntity> reservationEntities = jpaReservationRepository.findByUserId(userId);
        List<Reservation> userReservations = new ArrayList<>();
        for (ReservationEntity entity : reservationEntities) {
            userReservations.add(entity.toDomain());
        }

        // ReservationResponse로 변환 (공연장 정보 포함)
        List<ReservationResponse> reservationResponseList = new ArrayList<>();
        for (Reservation currentReservation : userReservations) {
            // 현재는 기존 Reservation 모델 구조를 유지하면서 기본 정보만 포함
            // TODO: Performance 기반 구조로 전환 시 공연장 정보 포함

            // 기본 콘서트 정보 생성 (기존 구조 유지)
            ReservationResponse.ConcertInfo concertInfo = new ReservationResponse.ConcertInfo(
                    currentReservation.getConcertDate(),
                    "콘서트 제목", // TODO: 실제 콘서트 제목 조회
                    currentReservation.getTicketPrice());

            // 기본 공연장 정보 생성 (임시)
            VenueInfo venueInfo = new VenueInfo(
                    "VENUE-001",
                    "서울 올림픽 경기장",
                    "서울",
                    "서울 송파구 올림픽로 424",
                    10000,
                    new ArrayList<>());

            ReservationResponse.SeatInfo seatInfo = new ReservationResponse.SeatInfo(
                    currentReservation.getSeatId(),
                    Integer.parseInt(currentReservation.getSeatId()));

            ReservationResponse reservationResponse = new ReservationResponse(
                    currentReservation.getReservationId(),
                    concertInfo,
                    venueInfo,
                    seatInfo,
                    currentReservation.getHoldExpiresAt().toString(),
                    currentReservation.getStatus().toString());

            reservationResponseList.add(reservationResponse);
        }

        return ResponseEntity.ok(reservationResponseList);
    }

    /**
     * 11. 결제 내역 조회 API
     */
    @GetMapping("/payments")
    // 사용자의 모든 결제 내역 조회
    public ResponseEntity<List<Payment>> getUserPayments(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        // 토큰 검증
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자의 결제 내역 조회
        List<PaymentEntity> paymentEntities = jpaPaymentRepository.findByUserId(userId);
        List<Payment> payments = new ArrayList<>();
        for (PaymentEntity entity : paymentEntities) {
            payments.add(entity.toDomain());
        }
        
        return ResponseEntity.ok(payments);
    }

    /**
     * 날짜와 제목으로 Performance 조회
     */
    private kr.hhplus.be.server.model.Performance findPerformanceByDateAndTitle(String concertDate,
            String concertTitle) {
        // 먼저 Concert를 찾아서 concertId를 얻음
        Optional<kr.hhplus.be.server.infrastructure.entity.ConcertEntity> concertOptional = jpaConcertRepository.findByDateAndTitle(concertDate, concertTitle);
        if (concertOptional.isEmpty()) {
            return null;
        }

        kr.hhplus.be.server.infrastructure.entity.ConcertEntity concertEntity = concertOptional.get();

        // 해당 Concert의 Performance들을 조회
        List<kr.hhplus.be.server.infrastructure.entity.PerformanceEntity> performanceEntities = jpaPerformanceRepository
                .findByConcertId(concertEntity.getConcertId());

        // 같은 날짜의 Performance 찾기
        for (kr.hhplus.be.server.infrastructure.entity.PerformanceEntity entity : performanceEntities) {
            if (entity.getPerformanceDate().toString().equals(concertDate)) {
                return entity.toDomain();
            }
        }

        return null;
    }

    /**
     * Performance로부터 Concert 이름 조회
     */
    private String getConcertNameByPerformance(kr.hhplus.be.server.model.Performance performance) {
        Optional<kr.hhplus.be.server.infrastructure.entity.ConcertEntity> concertOptional = jpaConcertRepository.findById(performance.getConcertId());
        return concertOptional.map(kr.hhplus.be.server.infrastructure.entity.ConcertEntity::getConcertName).orElse("알 수 없는 콘서트");
    }

    /**
     * 특정 공연의 예약 가능한 좌석 수 조회 (전통적인 Java 스타일 구현)
     */
    private int getAvailableSeatCount(String performanceId) {
        // 공연 정보 조회
        Optional<kr.hhplus.be.server.infrastructure.entity.PerformanceEntity> performanceOptional = jpaPerformanceRepository
                .findById(performanceId);
        if (performanceOptional.isEmpty()) {
            return 0;
        }

        kr.hhplus.be.server.infrastructure.entity.PerformanceEntity performanceEntity = performanceOptional.get();
        Optional<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueOptional = jpaVenueRepository
                .findById(performanceEntity.getVenueId());

        if (venueOptional.isEmpty()) {
            return 0;
        }

        kr.hhplus.be.server.infrastructure.entity.VenueEntity venueEntity = venueOptional.get();

        // 공연장의 총 좌석 수
        int totalSeatCount = venueEntity.getVenueCapacity();

        // TODO: 실제 예약된 좌석 수를 조회하여 차감
        // 현재는 임시로 45개 좌석이 예약 가능하다고 가정
        int reservedSeatCount = 5;

        return totalSeatCount - reservedSeatCount;
    }

    /**
     * 특정 공연의 예약된 좌석 수 조회
     */
    private int getReservedSeatCountForPerformance(String performanceId) {
        // 해당 공연의 예약된 좌석 수 조회
        List<kr.hhplus.be.server.infrastructure.entity.ReservationEntity> reservations = 
            jpaReservationRepository.findByPerformanceIdAndStatus(performanceId, "PAID");
        return reservations.size();
    }

    /**
     * 특정 콘서트의 공연장 정보 목록 조회
     */
    private List<VenueInfo> getVenueInfoListForConcert(String concertId) {
        // 해당 콘서트의 모든 공연 조회
        List<kr.hhplus.be.server.infrastructure.entity.PerformanceEntity> performanceEntities = jpaPerformanceRepository.findByConcertId(concertId);
        List<kr.hhplus.be.server.model.Performance> performanceList = new ArrayList<>();
        for (kr.hhplus.be.server.infrastructure.entity.PerformanceEntity entity : performanceEntities) {
            performanceList.add(entity.toDomain());
        }

        // 공연장별로 그룹화
        Map<String, List<kr.hhplus.be.server.model.Performance>> venuePerformanceMap = new HashMap<>();
        for (kr.hhplus.be.server.model.Performance currentPerformance : performanceList) {
            String venueId = currentPerformance.getVenueId();
            if (!venuePerformanceMap.containsKey(venueId)) {
                venuePerformanceMap.put(venueId, new ArrayList<>());
            }
            venuePerformanceMap.get(venueId).add(currentPerformance);
        }

        // 공연장 정보 목록 생성
        List<VenueInfo> venueInfoList = new ArrayList<>();
        for (Map.Entry<String, List<kr.hhplus.be.server.model.Performance>> entry : venuePerformanceMap.entrySet()) {
            String venueId = entry.getKey();
            List<kr.hhplus.be.server.model.Performance> venuePerformances = entry.getValue();

            // 공연장 정보 조회
            Optional<kr.hhplus.be.server.infrastructure.entity.VenueEntity> venueOptional = jpaVenueRepository.findById(venueId);
            if (venueOptional.isPresent()) {
                kr.hhplus.be.server.infrastructure.entity.VenueEntity venueEntity = venueOptional.get();

                // 공연 정보 목록 생성
                List<PerformanceInfo> performanceInfoList = new ArrayList<>();
                for (kr.hhplus.be.server.model.Performance currentPerformance : venuePerformances) {
                    int availableSeatCount = getAvailableSeatCount(currentPerformance.getPerformanceId());

                    PerformanceInfo performanceInfo = new PerformanceInfo(
                            currentPerformance.getPerformanceId(),
                            currentPerformance.getPerformanceDate(),
                            currentPerformance.getPerformanceTime(),
                            currentPerformance.getTicketPrice(),
                            availableSeatCount);
                    performanceInfoList.add(performanceInfo);
                }

                // 공연장 정보 생성
                VenueInfo venueInfo = new VenueInfo(
                        venueEntity.getVenueId(),
                        venueEntity.getVenueName(),
                        venueEntity.getVenueCity(),
                        venueEntity.getVenueAddress(),
                        venueEntity.getVenueCapacity(),
                        performanceInfoList);
                venueInfoList.add(venueInfo);
            }
        }

        return venueInfoList;
    }

}
