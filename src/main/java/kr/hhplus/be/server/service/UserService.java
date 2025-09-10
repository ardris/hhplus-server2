package kr.hhplus.be.server.service;

import java.math.BigDecimal;
import kr.hhplus.be.server.model.Transaction;
import java.util.List;

/**
 * 사용자 관리 서비스 인터페이스
 */
public interface UserService {
    
    /**
     * 사용자 잔액을 충전합니다.
     * @param tokenId 대기열 토큰 ID
     * @param amount 충전 금액
     * @return 충전 후 잔액
     */
    BigDecimal chargeBalance(String tokenId, BigDecimal amount);
    
    /**
     * 사용자 잔액을 조회합니다.
     * @param tokenId 대기열 토큰 ID
     * @return 현재 잔액
     */
    BigDecimal getBalance(String tokenId);
    
    /**
     * 사용자 잔액을 차감합니다.
     * @param userId 사용자 ID
     * @param amount 차감 금액
     */
    void deductBalance(String userId, BigDecimal amount);
    
    /**
     * 사용자 잔액이 충분한지 확인합니다.
     * @param userId 사용자 ID
     * @param amount 필요 금액
     * @return 잔액 충분 여부
     */
    boolean hasSufficientBalance(String userId, BigDecimal amount);
    
    /**
     * 사용자 잔액을 충전합니다. (거래 내역 포함)
     * @param tokenId 대기열 토큰 ID
     * @param amount 충전 금액
     * @return 충전 거래 정보
     */
    Transaction chargeBalanceWithTransaction(String tokenId, BigDecimal amount);
    
    /**
     * 사용자의 거래 내역을 조회합니다.
     * @param tokenId 대기열 토큰 ID
     * @param limit 최대 조회 개수
     * @return 거래 내역 목록
     */
    List<Transaction> getUserTransactions(String tokenId, int limit);
}
