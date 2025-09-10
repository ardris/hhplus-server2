package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.dto.request.BalanceChargeRequest;
import kr.hhplus.be.server.dto.request.ReservationRequest;
import kr.hhplus.be.server.dto.response.BalanceChargeResponse;
import kr.hhplus.be.server.dto.response.BalanceResponse;
import kr.hhplus.be.server.dto.response.PaymentResponse;
import kr.hhplus.be.server.dto.response.ReservationResponse;
import kr.hhplus.be.server.dto.response.TokenResponse;
import kr.hhplus.be.server.model.Concert;
import kr.hhplus.be.server.model.ConcertResponse;
import kr.hhplus.be.server.model.ConcertSeatResponse;
import kr.hhplus.be.server.model.Payment;
import kr.hhplus.be.server.model.Reservation;
import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.model.User;
import kr.hhplus.be.server.repository.*;
import kr.hhplus.be.server.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 1주차에 리뷰받은 내용 -> 컨트롤러 , 서비스 레이어의 구분을 명확히한다.
 * 
 * 컨트롤러는 요청을 받고, 서비스를 호출한다.
 * 서비스는 비즈니스 로직을 구현한다.
 * 저장소는 데이터를 저장하고 조회한다. (아직 JPA 쓰는건 아니겠지)
 *  * 
 * 콘서트 예약 서비스 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1")//버전으로 관리하는게 좋다고 함.
public class ReservationController {


    //각각의 서비스 전부 분리되어 있음.
    private final QueueService queueService;
    private final ConcertService concertService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final PaymentService paymentService;

    public ReservationController() {
        // 저장소 초기화 (인메모리 저장소) 추후 변경 할 부분임
        QueueTokenRepository queueTokenRepository = new QueueTokenRepository();
        ConcertRepository concertRepository = new ConcertRepository();
        SeatRepository seatRepository = new SeatRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        UserRepository userRepository = new UserRepository();
        PaymentRepository paymentRepository = new PaymentRepository();

        // 서비스 초기화
        this.queueService = new QueueServiceImpl(queueTokenRepository);
        this.concertService = new ConcertServiceImpl(concertRepository, seatRepository);
        this.reservationService = new ReservationServiceImpl(reservationRepository, seatRepository, queueService);
        this.userService = new UserServiceImpl(userRepository, queueService);
        this.paymentService = new PaymentServiceImpl(paymentRepository, reservationRepository, seatRepository,
                userService, queueService);

        this.concertService.initializeConcerts();
    }

    /**
     * 1. 대기열 토큰 발급 API
     */
    @PostMapping("/queue/token")
    public ResponseEntity<TokenResponse> issueQueueToken(@RequestParam String userId) {
        String tokenId = queueService.issueToken(userId);
        QueueService.QueueStatus queueStatus = queueService.getQueueStatus(tokenId);

        TokenResponse response = new TokenResponse(tokenId, queueStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 예약 가능한 콘서트 목록 조회 API (날짜 범위 필터링)
     */
    @GetMapping("/concerts")
    // 예약 가능한 콘서트 목록 조회 (날짜 범위 필터링 추가)
    public ResponseEntity<List<ConcertResponse>> getAvailableConcerts(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        queueService.validateToken(tokenId);
        
        List<Concert> concerts;
        
        // 날짜 범위가 제공된 경우 필터링된 결과 반환
        if (startDate != null && endDate != null) {
            concerts = concertService.getAvailableConcertsByDateRange(startDate, endDate);
        } else {
            concerts = concertService.getAvailableConcerts();
        }
        
        List<ConcertResponse> concertResponses = new ArrayList<>();
        for (Concert concert : concerts) {
            concertResponses.add(new ConcertResponse(
                concert.getDate(),
                concert.getTitle(),
                concert.getTicketPrice()
            ));
        }
        
        return ResponseEntity.ok(concertResponses);
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
     * 3. 특정 콘서트의 좌석 상세 정보 조회 API (개선된 버전)
     */
    @GetMapping("/concerts/seats/details")
    // 특정 콘서트의 좌석 상세 정보 조회 (콘서트 정보 + 좌석 상태 포함)
    public ResponseEntity<ConcertSeatResponse> getConcertSeatDetails(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @RequestParam String concertDate,
            @RequestParam String concertTitle) {
        queueService.validateToken(tokenId);
        ConcertSeatResponse response = concertService.getConcertSeatDetails(concertDate, concertTitle);
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
     * 4. 좌석 예약 요청 API (임시 배정) 중요 - 개선된 버전
     */
    @PostMapping("/reservations")
    // 좌석 예약 요청 (5분 임시 배정) - 콘서트 제목 포함
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
        
        // 좌석 임시 배정
        Reservation reservation = reservationService.holdSeat(
                tokenId, request.getConcertDate(), request.getConcertTitle(), request.getSeatId());
        
        // 콘서트 정보 조회
        Concert concert = concertRepository.findByDateAndTitle(request.getConcertDate(), request.getConcertTitle());
        
        // 응답 생성
        ReservationResponse.ConcertInfo concertInfo = new ReservationResponse.ConcertInfo(
            concert.getDate(),
            concert.getTitle(),
            concert.getTicketPrice()
        );
        
        ReservationResponse.SeatInfo seatInfo = new ReservationResponse.SeatInfo(
            request.getSeatId(),
            Integer.parseInt(request.getSeatId())
        );
        
        ReservationResponse response = new ReservationResponse(
            reservation.getReservationId(),
            concertInfo,
            seatInfo,
            reservation.getHoldExpiresAt().toString(),
            reservation.getStatus().toString()
        );
        
        return ResponseEntity.ok(response);
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
        Transaction transaction = userService.chargeBalanceWithTransaction(tokenId, request.getAmount());
        
        // 토큰에서 사용자 ID 조회
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        User user = userOptional.get();
        BigDecimal previousBalance = user.getBalance().subtract(request.getAmount());
        
        // 응답 생성
        BalanceChargeResponse response = new BalanceChargeResponse(
            userId,
            previousBalance,
            request.getAmount(),
            user.getBalance(),
            transaction.getTransactionId(),
            transaction.getCreatedAt().toString()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 6. 잔액 조회 API - 개선된 버전
     */
    @GetMapping("/balance")
    // 사용자 잔액 조회 (거래 내역 포함)
    public ResponseEntity<BalanceResponse> getBalance(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        User user = userOptional.get();
        
        // 최근 거래 내역 조회 (최대 10개)
        List<Transaction> recentTransactions = userService.getUserTransactions(tokenId, 10);
        
        // 거래 내역을 응답 형식으로 변환
        List<BalanceResponse.TransactionInfo> transactionInfos = new ArrayList<>();
        for (Transaction transaction : recentTransactions) {
            transactionInfos.add(new BalanceResponse.TransactionInfo(
                transaction.getTransactionId(),
                transaction.getType().toString(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getCreatedAt().toString()
            ));
        }
        
        // 응답 생성
        BalanceResponse response = new BalanceResponse(
            userId,
            user.getBalance(),
            user.getLastUpdatedAt().toString(),
            transactionInfos
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 7. 결제 처리 API - 개선된 버전
     */
    @PostMapping("/payments/{reservationId}")
    // 예약 결제 처리 (상세 정보 포함)
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @PathVariable String reservationId) {
        
        // 결제 처리 (거래 내역 포함)
        Payment payment = paymentService.processPaymentWithTransaction(tokenId, reservationId);
        
        // 예약 정보 조회
        Optional<Reservation> reservationOptional = reservationRepository.findByReservationId(reservationId);
        if (reservationOptional.isEmpty()) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
        
        Reservation reservation = reservationOptional.get();
        
        // 콘서트 정보 조회
        Concert concert = concertRepository.findByDateAndTitle(reservation.getConcertDate(), "콘서트 1일차"); // 임시
        
        // 토큰에서 사용자 ID 조회
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        User user = userOptional.get();
        BigDecimal balanceBefore = user.getBalance().add(payment.getAmount());
        
        // 응답 생성
        PaymentResponse.ReservationInfo reservationInfo = new PaymentResponse.ReservationInfo(
            reservation.getReservationId(),
            reservation.getConcertDate(),
            concert != null ? concert.getTitle() : "콘서트",
            Integer.parseInt(reservation.getSeatId())
        );
        
        PaymentResponse.PaymentInfo paymentInfo = new PaymentResponse.PaymentInfo(
            payment.getAmount(),
            payment.getStatus().toString(),
            payment.getCreatedAt().toString()
        );
        
        PaymentResponse.UserInfo userInfo = new PaymentResponse.UserInfo(
            userId,
            balanceBefore,
            user.getBalance()
        );
        
        PaymentResponse response = new PaymentResponse(
            payment.getPaymentId(),
            reservationInfo,
            paymentInfo,
            userInfo
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * 8. 대기열 상태 조회 API
     */
    @GetMapping("/queue/status")
    public ResponseEntity<QueueService.QueueStatus> getQueueStatus(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        QueueService.QueueStatus status = queueService.getQueueStatus(tokenId);
        return ResponseEntity.ok(status);
    }

    /**
     * 9. 예약 조회 API
     */
    @GetMapping("/reservations/{reservationId}")
    // 특정 예약 정보 조회
    public ResponseEntity<Reservation> getReservation(
            @RequestHeader("X-QUEUE-TOKEN") String tokenId,
            @PathVariable String reservationId) {
        Reservation reservation = reservationService.getReservation(tokenId, reservationId);
        return ResponseEntity.ok(reservation);
    }

    /**
     * 10. 사용자 예약 목록 조회 API
     */
    @GetMapping("/reservations")
    // 사용자의 모든 예약 목록 조회
    public ResponseEntity<List<Reservation>> getUserReservations(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        List<Reservation> reservations = reservationService.getUserReservations(tokenId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * 11. 결제 내역 조회 API
     */
    @GetMapping("/payments")
    // 사용자의 모든 결제 내역 조회
    public ResponseEntity<List<Payment>> getUserPayments(@RequestHeader("X-QUEUE-TOKEN") String tokenId) {
        List<Payment> payments = paymentService.getUserPayments(tokenId);
        return ResponseEntity.ok(payments);
    }

}
