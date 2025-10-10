package kr.hhplus.be.server.domain.port;

import kr.hhplus.be.server.model.User;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * 사용자 관련 포트 (클린 아키텍처 - 도메인 레이어)
 *
 * 핵심 개념:
 * 1. 도메인이 외부에 요구하는 사용자 관련 기능을 정의
 * 2. 인터페이스로 정의하여 구현체에 의존하지 않음
 * 3. 사용자 조회 및 잔액 관리 기능을 캡슐화
 */
public interface UserPort {
    /**
     * 사용자 ID로 조회
     * @param userId 사용자 ID
     * @return 사용자 정보 (없으면 Optional.empty())
     */
    Optional<User> findById(String userId);

    /**
     * 사용자 잔액 충분 여부 확인
     * @param userId 사용자 ID
     * @param amount 확인할 금액
     * @return 잔액 충분 여부
     */
    boolean hasEnoughBalance(String userId, BigDecimal amount);

    /**
     * 사용자 잔액 차감
     * @param userId 사용자 ID
     * @param amount 차감할 금액
     */
    void deductBalance(String userId, BigDecimal amount);
}
