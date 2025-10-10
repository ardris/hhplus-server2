package kr.hhplus.be.server.domain.port;

import kr.hhplus.be.server.domain.entity.Payment;
import java.util.List;
import java.util.Optional;

/**
 * 결제 저장소 포트 (클린 아키텍처 - 도메인 레이어)
 * 
 * 핵심 개념:
 * 1. 도메인이 외부에 요구하는 기능을 정의
 * 2. 인터페이스로 정의하여 구현체에 의존하지 않음
 * 3. 도메인 엔티티를 그대로 사용 (JPA 엔티티가 아님)
 */
public interface PaymentRepositoryPort {
    /**
     * 결제 저장
     * @param payment 도메인 엔티티
     * @return 저장된 도메인 엔티티
     */
    Payment save(Payment payment);
    
    /**
     * ID로 결제 조회
     * @param paymentId 결제 ID
     * @return 결제 엔티티 (없으면 Optional.empty())
     */
    Optional<Payment> findById(String paymentId);
    
    /**
     * 사용자 ID로 결제 목록 조회
     * @param userId 사용자 ID
     * @return 결제 목록
     */
    List<Payment> findByUserId(String userId);
    
    /**
     * 예약 ID로 결제 조회
     * @param reservationId 예약 ID
     * @return 결제 엔티티 (없으면 Optional.empty())
     */
    Optional<Payment> findByReservationId(String reservationId);
}
