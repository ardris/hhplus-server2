package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.QueueToken;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 대기열 토큰을 관리하는 인메모리 저장소 추후 DB 연동 , Redis 사용해보자
 */
public class QueueTokenRepository {
    

    //전체 토큰 관리
    private final Map<String, QueueToken> tokenStore = new ConcurrentHashMap<>();

    //사용자 별 최신 토큰
    private final Map<String, String> latestTokenByUserId = new ConcurrentHashMap<>();
    
    // 토큰 저장
    public QueueToken save(QueueToken queueToken) {

    
        tokenStore.put(queueToken.getTokenId(), queueToken);
        latestTokenByUserId.put(queueToken.getUserId(), queueToken.getTokenId());
        return queueToken;
    }
    
    // 토큰 ID로 토큰 조회
    public Optional<QueueToken> findByTokenId(String tokenId) {
        return Optional.ofNullable(tokenStore.get(tokenId));
    }
    
    // 사용자의 최신 토큰 조회
    public Optional<QueueToken> findLatestTokenByUserId(String userId) {
        String latestTokenId = latestTokenByUserId.get(userId);
        if (latestTokenId == null) {
            return Optional.empty();
        }
        return findByTokenId(latestTokenId);
    }
    
    // 활성 토큰 목록 조회
    public List<QueueToken> findActiveTokens() {
        List<QueueToken> activeTokens = new ArrayList<>();
        for (QueueToken token : tokenStore.values()) {
            if (token.isActive()) {
                activeTokens.add(token);
            }
        }
        return activeTokens;
    }
    
    // 만료된 토큰 목록 조회
    public List<QueueToken> findExpiredTokens() {
        List<QueueToken> expiredTokens = new ArrayList<>();
        for (QueueToken token : tokenStore.values()) {
            if (token.isExpired()) {
                expiredTokens.add(token);
            }
        }
        return expiredTokens;
    }
    
    // 토큰 정보 업데이트
    public void update(QueueToken queueToken) {
        tokenStore.put(queueToken.getTokenId(), queueToken);
    }
    
    // 토큰 삭제
    public void deleteByTokenId(String tokenId) {
        QueueToken token = tokenStore.remove(tokenId);
        if (token != null) {
            String latestTokenId = latestTokenByUserId.get(token.getUserId());
            if (tokenId.equals(latestTokenId)) {
                latestTokenByUserId.remove(token.getUserId());
            }
        }
    }
    
    // 사용자의 모든 토큰 삭제
    public void deleteByUserId(String userId) {
        String latestTokenId = latestTokenByUserId.remove(userId);
        if (latestTokenId != null) {
            tokenStore.remove(latestTokenId);
        }
    }
    
    // 토큰 존재 여부 확인
    public boolean existsByTokenId(String tokenId) {
        return tokenStore.containsKey(tokenId);
    }
    
    // 사용자의 활성 토큰 존재 여부 확인
    public boolean hasActiveToken(String userId) {
        Optional<QueueToken> latestToken = findLatestTokenByUserId(userId);
        if (latestToken.isPresent()) {
            return latestToken.get().isValid();
        }
        return false;
    }
}
