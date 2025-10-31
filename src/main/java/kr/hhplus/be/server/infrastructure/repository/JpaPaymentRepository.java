package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 결제 JPA Repository
 * 
 * Infrastructure Layer에서 데이터베이스와 직접 소통하는 Repository입니다.
 * JPA를 사용하여 CRUD 작업을 수행합니다.
 */
@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, String> {
    
    /**
     * 사용자 ID로 결제 목록 조회
     */
    List<PaymentEntity> findByUserId(String userId);
    
    /**
     * 예약 ID로 결제 조회
     */
    Optional<PaymentEntity> findByReservationId(String reservationId);
    
    
    /**
     * 사용자의 결제 내역 조회 (최신순)
     */
    @Query("SELECT p FROM PaymentEntity p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    
    /**
     * 결제 상태별 조회
     */
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = :status")
    List<PaymentEntity> findByStatus(@Param("status") String status);
    
    /**
     * 결제 방법별 조회
     */
    @Query("SELECT p FROM PaymentEntity p WHERE p.paymentMethod = :paymentMethod")
    List<PaymentEntity> findByPaymentMethod(@Param("paymentMethod") String paymentMethod);
}
