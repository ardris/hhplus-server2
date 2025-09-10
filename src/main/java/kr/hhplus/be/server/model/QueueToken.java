package kr.hhplus.be.server.model;

import java.time.LocalDateTime;

/**
 * 대기열 토큰을 관리하는 도메인 모델
 * 
 * 콘서트 예약 서비스에서 사용자의 대기열 접근 권한을 관리하는 클래스입니다.
 * 토큰 발급부터 활성화, 만료까지의 전체 라이프사이클을 추적하고,
 * 대기열 순서와 활성화 상태를 관리하여 서비스 접근을 제어합니다.
 */
public class QueueToken {

    private String tokenId;               // 토큰 고유 식별자
    private String userId;                // 토큰 소유자 사용자 ID
    private int queuePosition;            // 발급 시점의 대기 순번 (FIFO 순서 보장)
    private LocalDateTime issuedAt;       // 토큰 발급 시간
    private LocalDateTime expiresAt;      // 토큰 만료 시간 (15분 후)
    private boolean isActive;             // 활성 상태 여부 (true: 서비스 이용 가능, false: 대기 중)
    private LocalDateTime activatedAt;    // 활성화 시간 (서비스 이용 시작 시점)

    /**
     * 대기열 토큰 객체를 생성합니다.
     * 
     * @param tokenId 토큰 고유 식별자
     * @param userId 토큰 소유자 사용자 ID
     * @param queuePosition 발급 시점의 대기 순번
     * 
     * 이유: 사용자가 대기열에 진입했을 때 토큰을 발급할 때 호출됩니다.
     * 생성 시 isActive는 false로 설정되어 대기 상태가 되고,
     * expiresAt은 15분 후로 설정되어 토큰의 유효 기간을 관리합니다.
     * queuePosition을 통해 FIFO 순서를 보장하여 공정한 대기열을 구현합니다.
     */
    // 토큰 객체 생성 (기본 8분)
    public QueueToken(String tokenId, String userId, int queuePosition) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.queuePosition = queuePosition;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(15); // 15분 유효
        this.isActive = false;  // 기본적으로 대기 상태
    }
    
    /**
     * 토큰 유효 시간을 지정하여 생성합니다.
     * 
     * @param tokenId 토큰 고유 식별자
     * @param userId 토큰 소유자 사용자 ID
     * @param queuePosition 발급 시점의 대기 순번
     * @param validityMinutes 토큰 유효 시간 (분 단위)
     * 
     * 이유: QueueServiceImpl에서 정의한 TOKEN_VALIDITY_MINUTES 상수를 사용하여
     * 토큰 유효 시간을 일관성 있게 관리할 수 있도록 합니다.
     */
    // 토큰 객체 생성 (유효시간 지정)
    public QueueToken(String tokenId, String userId, int queuePosition, int validityMinutes) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.queuePosition = queuePosition;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(validityMinutes);
        this.isActive = false;  // 기본적으로 대기 상태
    }

    /**
     * 토큰을 활성화합니다.
     * 
     * 이유: 사용자의 차례가 되어 서비스를 이용할 수 있게 되었을 때 호출됩니다.
     * isActive를 true로 변경하여 해당 사용자가 예약 서비스를 이용할 수 있게 만듭니다.
     * activatedAt 시간을 기록하여 활성화 시점을 추적할 수 있게 합니다.
     * 활성화된 토큰은 모든 API 호출에서 유효성 검증을 통과할 수 있습니다.
     */
    // 토큰 활성화
    public void activate() {
        this.isActive = true;
        this.activatedAt = LocalDateTime.now();
    }

    /**
     * 토큰을 비활성화합니다.
     * 
     * 이유: 사용자가 결제를 완료하거나, 서비스 이용을 종료했을 때 호출됩니다.
     * isActive를 false로 변경하여 해당 사용자가 더 이상 서비스를 이용할 수 없게 만듭니다.
     * 비활성화된 토큰은 API 호출 시 유효성 검증에서 실패하게 됩니다.
     * 이렇게 하여 한 번에 활성화된 사용자 수를 제한할 수 있습니다.
     */
    // 토큰 비활성화
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     * 
     * @return true: 만료됨, false: 아직 유효함
     * 
     * 이유: 토큰의 유효 기간(15분)이 지났는지 확인할 때 사용됩니다.
     * 만료된 토큰은 더 이상 사용할 수 없으며, 새로운 토큰을 발급받아야 합니다.
     * API 호출 시 토큰 유효성을 검증하거나, 정리 작업에서 만료된 토큰을 제거할 때 활용됩니다.
     */
    // 토큰 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 토큰이 유효한지 확인합니다.
     * 
     * @return true: 유효함 (만료되지 않았고 활성화됨), false: 유효하지 않음
     * 
     * 이유: API 호출 시 토큰의 전체적인 유효성을 검증할 때 사용됩니다.
     * 토큰이 만료되지 않았고(isExpired() == false) 동시에 활성화된 상태(isActive == true)여야 유효합니다.
     * 이 두 조건을 모두 만족해야 사용자가 서비스를 이용할 수 있습니다.
     */
    // 토큰 유효성 확인
    public boolean isValid() {
        return !isExpired() && this.isActive;
    }

    /**
     * 토큰의 남은 유효 시간을 초 단위로 반환합니다.
     * 
     * @return 남은 유효 시간 (초 단위), 만료된 경우 0
     * 
     * 이유: 사용자에게 토큰의 남은 유효 시간을 알려주어 언제까지 서비스를 이용할 수 있는지 안내할 때 사용됩니다.
     * UI에서 "남은 시간: 5분 30초"와 같은 형태로 표시하거나,
     * 토큰 만료 전에 사용자에게 알림을 보낼 때 활용됩니다.
     * 만료된 토큰의 경우 0을 반환하여 명확하게 만료 상태를 나타냅니다.
     */
    // 토큰 남은 유효 시간 조회
    public long getRemainingTimeInSeconds() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.expiresAt).getSeconds();
    }

    // Getters and Setters
    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }
}
