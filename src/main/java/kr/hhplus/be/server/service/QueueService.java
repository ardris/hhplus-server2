package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.QueueToken;

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
     * 토큰 유효성 검증 (사용자 ID와 토큰 ID로)
     * 
     * @param userId 사용자 ID
     * @param tokenId 토큰 ID
     * @return 유효한 토큰인지 여부
     */
    boolean isTokenValid(String userId, String tokenId);

    /**
     * 사용자를 활성 상태로 만듭니다.
     * 
     * @param userId 사용자 ID
     */
    void activateUser(String userId);

    /**
     * 사용자가 활성 상태인지 확인합니다.
     * 
     * @param userId 사용자 ID
     * @return 활성 상태인지 여부
     */
    boolean isUserActive(String userId);

    /**
     * 토큰을 만료시킵니다.
     * 
     * @param tokenId 토큰 ID
     */
    void expireToken(String tokenId);

    /**
     * 사용자를 대기열에서 제거합니다.
     * 
     * @param userId 사용자 ID
     */
    void removeUserFromQueue(String userId);

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
