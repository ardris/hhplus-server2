package kr.hhplus.be.server.service;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import kr.hhplus.be.server.exception.QueueTokenException;
import kr.hhplus.be.server.model.QueueToken;
import kr.hhplus.be.server.repository.QueueTokenRepository;

/**
 * 대기열 관리 서비스 구현체
 */
@Service
public class QueueServiceImpl implements QueueService {

    private static final int MAX_ACTIVE_USERS = 100;
    private static final int TOKEN_VALIDITY_MINUTES = 8;

    private final QueueTokenRepository queueTokenRepository;
    private final Queue<String> waitingUserQueue = new ConcurrentLinkedQueue<>(); // FIFO 대기열
    private final Set<String> activeUserSet = ConcurrentHashMap.newKeySet(); // 활성 사용자 집합
    private final Set<String> enqueuedUserSet = ConcurrentHashMap.newKeySet(); // 대기열 사용자 집합 ㄴet 사용으로 여러번 대기열 들어가는걸
                                                                               // 방지하려고 함.
    private final AtomicInteger queuePositionCounter = new AtomicInteger(1); // 순서 지정입니다. atomic 은 여러 사용자가 들어와도 순서 보장할려고
                                                                             // 썼습니다.
    private final ReentrantLock promotionLock = new ReentrantLock(); // 락

    public QueueServiceImpl(QueueTokenRepository queueTokenRepository) {
        this.queueTokenRepository = queueTokenRepository;
    }

    @Override
    // 대기열 토큰 발급
    public String issueToken(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        // 기존 유효한 토큰이 있는지 확인
        Optional<QueueToken> existingTokenOptional = queueTokenRepository.findLatestTokenByUserId(userId);
        if (existingTokenOptional.isPresent()) {
            QueueToken existingToken = existingTokenOptional.get();
            if (existingToken.isValid()) {
                return existingToken.getTokenId();
            }
        }

        // 새 토큰 발급

        // 토큰은 uuid 랜덤으로 만듭니다.
        String tokenId = UUID.randomUUID().toString();

        // 멀티스레드에서 안전하게 순서를 증가시키기 위해 Atomic 사용! CAS(Compare-And-Swap) 연산
        int queuePosition = queuePositionCounter.getAndIncrement();

        // 토큰을 생성합니다. 상수 값 우선 8로함
        QueueToken newToken = new QueueToken(tokenId, userId, queuePosition, TOKEN_VALIDITY_MINUTES);

        // 인메모리 DB 인 Token Repository 에 세이브!
        queueTokenRepository.save(newToken);

        // 대기열에 추가
        if (enqueuedUserSet.add(userId)) {
            // 맨 뒤에 추가
            waitingUserQueue.offer(userId);
        }

        // 대기열 업데이트
        promoteUsers();

        return tokenId;
    }

    @Override
    // 토큰 유효성 검증
    public QueueToken validateToken(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            throw new QueueTokenException("토큰 ID는 필수입니다.");
        }

        Optional<QueueToken> tokenOptional = queueTokenRepository.findByTokenId(tokenId);
        if (!tokenOptional.isPresent()) {
            throw new QueueTokenException.InvalidTokenException();
        }
        QueueToken token = tokenOptional.get();

        if (token.isExpired()) {
            queueTokenRepository.deleteByTokenId(tokenId);
            throw new QueueTokenException.TokenExpiredException();
        }

        // 최신 토큰인지 확인
        Optional<QueueToken> latestTokenOptional = queueTokenRepository.findLatestTokenByUserId(token.getUserId());
        if (!latestTokenOptional.isPresent() || !latestTokenOptional.get().getTokenId().equals(tokenId)) {
            throw new QueueTokenException.LatestTokenMismatchException();
        }

        // 대기열 승격 시도
        promoteUsers();

        return token;
    }

    @Override
    // 대기열 상태 조회
    public QueueStatus getQueueStatus(String tokenId) {
        QueueToken token = validateToken(tokenId);

        int totalWaitingUsers = waitingUserQueue.size() + activeUserSet.size();
        boolean isActive = activeUserSet.contains(token.getUserId());
        long remainingTime = token.getRemainingTimeInSeconds();

        return new QueueStatus(token.getQueuePosition(), totalWaitingUsers, isActive, remainingTime);
    }

    @Override
    // 만료된 토큰 정리
    public void cleanupExpiredTokens() {
        List<QueueToken> expiredTokens = queueTokenRepository.findExpiredTokens();

        for (QueueToken token : expiredTokens) {
            queueTokenRepository.deleteByTokenId(token.getTokenId());
            activeUserSet.remove(token.getUserId());
            enqueuedUserSet.remove(token.getUserId());
        }

        // 대기열 승격 시도
        promoteUsers();
    }

    /**
     * 대기열에서 사용자를 활성화시킵니다.
     */
    // 대기열에서 사용자 활성화
    private void promoteUsers() {
        if (!promotionLock.tryLock()) {
            return;// 다른 스레드가 이미 처리 중
        }

        try {
            // 최대 활성 사용자 수(100명)까지 대기열에서 승인

            while (activeUserSet.size() < MAX_ACTIVE_USERS && !waitingUserQueue.isEmpty()) {
                String userId = waitingUserQueue.poll();
                if (userId != null) {

                    // 활성된 사용자 set 추가
                    activeUserSet.add(userId);
                    // 대기열 삭제!
                    enqueuedUserSet.remove(userId);

                    // 토큰 활성화
                    Optional<QueueToken> tokenOptional = queueTokenRepository.findLatestTokenByUserId(userId);
                    if (tokenOptional.isPresent()) {
                        QueueToken token = tokenOptional.get();
                        token.activate();

                        queueTokenRepository.update(token);
                    }
                }
            }
        } finally {
            promotionLock.unlock();
        }
    }

    /**
     * 사용자를 대기열에서 제거합니다.
     */
    // 사용자를 대기열에서 제거
    public void removeUserFromQueue(String userId) {
        activeUserSet.remove(userId);
        enqueuedUserSet.remove(userId);

        // 토큰 비활성화
        Optional<QueueToken> tokenOptional = queueTokenRepository.findLatestTokenByUserId(userId);
        if (tokenOptional.isPresent()) {
            QueueToken token = tokenOptional.get();
            token.deactivate();
            queueTokenRepository.update(token);
        }

        // 대기열 승격 시도
        promoteUsers();
    }

    /**
     * 사용자가 활성 상태인지 확인합니다.
     */
    // 사용자 활성 상태 확인
    public boolean isUserActive(String userId) {
        return activeUserSet.contains(userId);
    }

    @Override
    public boolean isTokenValid(String userId, String tokenId) {
        try {
            QueueToken token = validateToken(tokenId);
            return token.getUserId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void activateUser(String userId) {
        if (activeUserSet.size() < MAX_ACTIVE_USERS) {
            activeUserSet.add(userId);
            enqueuedUserSet.remove(userId);
            waitingUserQueue.remove(userId);
        }
    }

    @Override
    public void expireToken(String tokenId) {
        try {
            QueueToken token = validateToken(tokenId);
            token.deactivate();
            queueTokenRepository.update(token);
            removeUserFromQueue(token.getUserId());
        } catch (Exception e) {
            // 토큰이 이미 만료되었거나 존재하지 않는 경우 무시
        }
    }
}
