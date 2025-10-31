package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.domain.port.PaymentRepositoryPort;
import kr.hhplus.be.server.infrastructure.entity.PaymentEntity;
import kr.hhplus.be.server.infrastructure.repository.JpaPaymentRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 결제 데이터 저장/조회 어댑터
 * 
 * PaymentRepositoryPort 인터페이스를 구현해서 도메인 레이어와 데이터베이스를 연결해.
 * 클린아키텍처의 인프라스트럭처 레이어에서 포트를 구현하는 역할을 해.
 */
@Primary
@Repository
@Transactional
public class JpaPaymentAdapter implements PaymentRepositoryPort {
    private final JpaPaymentRepository jpaPaymentRepository;

    /**
     * JPA Repository를 주입받아 사용해.
     * 도메인 레이어는 저장소에 직접 의존하지 않고, 이 어댑터를 통해 간접적으로 접근해.
     */
    public JpaPaymentAdapter(JpaPaymentRepository jpaPaymentRepository) {
        this.jpaPaymentRepository = jpaPaymentRepository;
    }

    @Override
    public Payment save(Payment payment) {
        // 도메인 엔티티를 JPA 엔티티로 변환하여 저장
        PaymentEntity entity = PaymentEntity.fromDomain(payment);
        PaymentEntity savedEntity = jpaPaymentRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        Optional<PaymentEntity> entity = jpaPaymentRepository.findById(paymentId);
        return entity.map(PaymentEntity::toDomain);
    }

    @Override
    public List<Payment> findByUserId(String userId) {
        List<PaymentEntity> entities = jpaPaymentRepository.findByUserId(userId);
        List<Payment> payments = new ArrayList<>();
        for (PaymentEntity entity : entities) {
            payments.add(entity.toDomain());
        }
        return payments;
    }

    @Override
    public Optional<Payment> findByReservationId(String reservationId) {
        Optional<PaymentEntity> entity = jpaPaymentRepository.findByReservationId(reservationId);
        return entity.map(PaymentEntity::toDomain);
    }
}