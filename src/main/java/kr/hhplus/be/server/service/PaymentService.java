package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Payment;
import kr.hhplus.be.server.model.Transaction;

/**
 * 결제 관리 서비스 인터페이스
 */
public interface PaymentService {
    
    /**
     * 예약에 대한 결제를 처리합니다.
     * @param tokenId 대기열 토큰 ID
     * @param reservationId 예약 ID
     * @return 결제 정보
     */
    Payment processPayment(String tokenId, String reservationId);
    
    /**
     * 결제 정보를 조회합니다.
     * @param tokenId 대기열 토큰 ID
     * @param paymentId 결제 ID
     * @return 결제 정보
     */
    Payment getPayment(String tokenId, String paymentId);
    
    /**
     * 사용자의 결제 내역을 조회합니다.
     * @param tokenId 대기열 토큰 ID
     * @return 결제 내역 목록
     */
    java.util.List<Payment> getUserPayments(String tokenId);
    
    /**
     * 예약에 대한 결제를 처리합니다. (거래 내역 포함)
     * @param tokenId 대기열 토큰 ID
     * @param reservationId 예약 ID
     * @return 결제 정보와 거래 내역
     */
    Payment processPaymentWithTransaction(String tokenId, String reservationId);
}
