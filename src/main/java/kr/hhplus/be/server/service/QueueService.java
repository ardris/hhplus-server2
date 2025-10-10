package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.QueueToken;
import java.util.List;

/**
 * 대기열 관리 서비스 인터페이스
 */
public interface QueueService {

    /**
     * 사용자에게 대기열 토큰을 발급합니다.
     * 
     * @param userId 사용자 ID
     * @return 발급된 토큰 ID
     */
    String issueToken(String userId);

    /**
     * 토큰의 유효성을 검증하고 필요시 대기열을 승격시킵니다.
     * 
     * @param tokenId 토큰 ID
     * @return 유효한 토큰 정보
     */
    QueueToken validateToken(String tokenId);

    /**
     * 사용자의 대기열 상태를 조회합니다.
     * 
     * @param tokenId 토큰 ID
     * @return 대기열 상태 정보
     */
    QueueStatus getQueueStatus(String tokenId);

    /**
     * 만료된 토큰들을 정리합니다.
     */
    void cleanupExpiredTokens();

    /**
     * 대기열 상태 정보
     */
    class QueueStatus {
        private final int queuePosition;
        private final int totalWaitingUsers;
        private final boolean isActive;
        private final long remainingTimeInSeconds;

        public QueueStatus(int queuePosition, int totalWaitingUsers, boolean isActive, long remainingTimeInSeconds) {
            this.queuePosition = queuePosition;
            this.totalWaitingUsers = totalWaitingUsers;
            this.isActive = isActive;
            this.remainingTimeInSeconds = remainingTimeInSeconds;
        }

        public int getQueuePosition() {
            return queuePosition;
        }

        public int getTotalWaitingUsers() {
            return totalWaitingUsers;
        }

        public boolean isActive() {
            return isActive;
        }

        public long getRemainingTimeInSeconds() {
            return remainingTimeInSeconds;
        }
    }
}
