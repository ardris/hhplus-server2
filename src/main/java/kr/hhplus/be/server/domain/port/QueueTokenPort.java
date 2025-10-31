package kr.hhplus.be.server.domain.port;

import kr.hhplus.be.server.model.QueueToken;

import java.util.List;
import java.util.Optional;

/**
 * 대기열 토큰 관련 포트 (클린 아키텍처 - 도메인 레이어)
 *
 * 핵심 개념:
 * 1. 도메인이 외부에 요구하는 대기열 토큰 관련 기능을 정의
 * 2. 인터페이스로 정의하여 구현체에 의존하지 않음
 * 3. 대기열 토큰 관리 기능을 캡슐화
 */
public interface QueueTokenPort {
    /**
     * 토큰 저장
     * @param queueToken 저장할 토큰
     * @return 저장된 토큰
     */
    QueueToken save(QueueToken queueToken);

    /**
     * 토큰 ID로 토큰 조회
     * @param tokenId 토큰 ID
     * @return 토큰 정보
     */
    Optional<QueueToken> findByTokenId(String tokenId);

    /**
     * 사용자의 최신 토큰 조회
     * @param userId 사용자 ID
     * @return 최신 토큰 정보
     */
    Optional<QueueToken> findLatestTokenByUserId(String userId);

    /**
     * 활성 토큰 목록 조회
     * @return 활성 토큰 목록
     */
    List<QueueToken> findActiveTokens();

    /**
     * 만료된 토큰 목록 조회
     * @return 만료된 토큰 목록
     */
    List<QueueToken> findExpiredTokens();

    /**
     * 토큰 정보 업데이트
     * @param queueToken 업데이트할 토큰
     */
    void updateToken(QueueToken queueToken);

    /**
     * 만료된 토큰 삭제
     */
    void deleteExpiredTokens();
}

