package kr.hhplus.be.server.dto.response;

import kr.hhplus.be.server.service.QueueService;

public class TokenResponse {
    private final String tokenId;
    private final QueueService.QueueStatus queueStatus;

    public TokenResponse(String tokenId, QueueService.QueueStatus queueStatus) {
        this.tokenId = tokenId;
        this.queueStatus = queueStatus;
    }

    public String getTokenId() {
        return tokenId;
    }

    public QueueService.QueueStatus getQueueStatus() {
        return queueStatus;
    }
}
