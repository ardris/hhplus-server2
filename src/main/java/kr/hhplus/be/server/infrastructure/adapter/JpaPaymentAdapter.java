package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.entity.Payment;
import kr.hhplus.be.server.domain.port.PaymentRepositoryPort;
import kr.hhplus.be.server.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 기존 Repository를 사용하여 결제 데이터를 저장하고 조회하는 어댑터입니다.
 * PaymentRepositoryPort 인터페이스를 구현하여 도메인 레이어와 기존 Repository를 연결합니다.
 */
@Repository
public class JpaPaymentAdapter implements PaymentRepositoryPort {
    private final PaymentRepository paymentRepository;

    /**
     * 기존 Repository를 주입받아 사용합니다.
     * 도메인 레이어는 기존 Repository에 직접 의존하지 않고, 이 어댑터를 통해 간접적으로 접근합니다.
     */
    public JpaPaymentAdapter(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment save(Payment payment) {
        // 도메인 엔티티를 기존 모델로 변환하여 저장
        kr.hhplus.be.server.model.Payment paymentModel = convertToModel(payment);
        kr.hhplus.be.server.model.Payment savedPaymentModel = paymentRepository.save(paymentModel);

        // 저장된 모델을 다시 도메인 엔티티로 변환하여 반환
        return convertToDomain(savedPaymentModel);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        Optional<kr.hhplus.be.server.model.Payment> paymentModelOptional = paymentRepository.findById(paymentId);
        if (paymentModelOptional.isPresent()) {
            return Optional.of(convertToDomain(paymentModelOptional.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Payment> findByUserId(String userId) {
        List<kr.hhplus.be.server.model.Payment> paymentModelList = paymentRepository.findByUserId(userId);
        List<Payment> paymentDomainList = new ArrayList<>();
        for (kr.hhplus.be.server.model.Payment paymentModel : paymentModelList) {
            paymentDomainList.add(convertToDomain(paymentModel));
        }
        return paymentDomainList;
    }

    @Override
    public Optional<Payment> findByReservationId(String reservationId) {
        Optional<kr.hhplus.be.server.model.Payment> paymentModelOptional = paymentRepository
                .findByReservationId(reservationId);
        if (paymentModelOptional.isPresent()) {
            return Optional.of(convertToDomain(paymentModelOptional.get()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * 도메인 엔티티를 기존 모델로 변환
     */
    private kr.hhplus.be.server.model.Payment convertToModel(Payment paymentDomain) {
        // TODO: 도메인 엔티티를 기존 모델로 변환하는 로직 구현
        return new kr.hhplus.be.server.model.Payment();
    }

    /**
     * 기존 모델을 도메인 엔티티로 변환
     */
    private Payment convertToDomain(kr.hhplus.be.server.model.Payment paymentModel) {
        // TODO: 기존 모델을 도메인 엔티티로 변환하는 로직 구현
        return Payment.create(
                paymentModel.getUserId(),
                paymentModel.getReservationId(),
                paymentModel.getAmount());
    }
}
