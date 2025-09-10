package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.Payment;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 결제 정보를 관리하는 인메모리 저장소
 */
public class PaymentRepository {
    
    private final Map<String, Payment> paymentStore = new ConcurrentHashMap<>();
    
    public Payment save(Payment payment) {
        paymentStore.put(payment.getPaymentId(), payment);
        return payment;
    }
    
    public Optional<Payment> findByPaymentId(String paymentId) {
        return Optional.ofNullable(paymentStore.get(paymentId));
    }
    
    public List<Payment> findByUserId(String userId) {
        List<Payment> userPayments = new ArrayList<>();
        for (Payment payment : paymentStore.values()) {
            if (payment.getUserId().equals(userId)) {
                userPayments.add(payment);
            }
        }
        return userPayments;
    }
    
    public List<Payment> findByReservationId(String reservationId) {
        List<Payment> payments = new ArrayList<>();
        for (Payment payment : paymentStore.values()) {
            if (payment.getReservationId().equals(reservationId)) {
                payments.add(payment);
            }
        }
        return payments;
    }
    
    public Optional<Payment> findCompletedPaymentByReservationId(String reservationId) {
        for (Payment payment : findByReservationId(reservationId)) {
            if (payment.isCompleted()) {
                return Optional.of(payment);
            }
        }
        return Optional.empty();
    }
    
    public void update(Payment payment) {
        paymentStore.put(payment.getPaymentId(), payment);
    }
    
    public boolean existsByPaymentId(String paymentId) {
        return paymentStore.containsKey(paymentId);
    }
    
    public void deleteByPaymentId(String paymentId) {
        paymentStore.remove(paymentId);
    }
}
