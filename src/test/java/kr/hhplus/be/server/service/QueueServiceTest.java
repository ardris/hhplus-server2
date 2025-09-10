package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.QueueTokenException;
import kr.hhplus.be.server.model.QueueToken;
import kr.hhplus.be.server.repository.QueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 대기열 서비스 단위 테스트
 */
class QueueServiceTest {

    private QueueService queueService;
    private QueueTokenRepository queueTokenRepository;

    @BeforeEach
    void setUp() {
        queueTokenRepository = new QueueTokenRepository();
        queueService = new QueueServiceImpl(queueTokenRepository);
    }

    @Test
    void 토큰_발급_성공() {
        // Given
        String userId = "user123";

        // When
        String tokenId = queueService.issueToken(userId);

        // Then
        assertNotNull(tokenId);
        assertFalse(tokenId.isEmpty());
        
        QueueToken token = queueTokenRepository.findByTokenId(tokenId).orElse(null);
        assertNotNull(token);
        assertEquals(userId, token.getUserId());
        assertFalse(token.isExpired());
    }

    @Test
    void 동일_사용자_토큰_재발급_시_기존_토큰_반환() {
        // Given
        String userId = "user123";
        String firstTokenId = queueService.issueToken(userId);

        // When
        String secondTokenId = queueService.issueToken(userId);

        // Then
        assertEquals(firstTokenId, secondTokenId);
    }

    @Test
    void 토큰_검증_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);

        // When
        QueueToken validatedToken = queueService.validateToken(tokenId);

        // Then
        assertNotNull(validatedToken);
        assertEquals(userId, validatedToken.getUserId());
        assertEquals(tokenId, validatedToken.getTokenId());
    }

    @Test
    void 유효하지_않은_토큰_검증_실패() {
        // Given
        String invalidTokenId = "invalid-token";

        // When & Then
        assertThrows(QueueTokenException.InvalidTokenException.class, () -> {
            queueService.validateToken(invalidTokenId);
        });
    }

    @Test
    void 대기열_상태_조회_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);

        // When
        QueueService.QueueStatus status = queueService.getQueueStatus(tokenId);

        // Then
        assertNotNull(status);
        assertTrue(status.getQueuePosition() > 0);
        assertTrue(status.getTotalWaitingUsers() >= 0);
        assertNotNull(status.getRemainingTimeInSeconds());
    }

    @Test
    void 사용자_ID_없이_토큰_발급_실패() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            queueService.issueToken(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            queueService.issueToken("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            queueService.issueToken("   ");
        });
    }

    @Test
    void 토큰_ID_없이_검증_실패() {
        // When & Then
        assertThrows(QueueTokenException.class, () -> {
            queueService.validateToken(null);
        });

        assertThrows(QueueTokenException.class, () -> {
            queueService.validateToken("");
        });
    }

    @Test
    void 사용자_대기열_활성화_테스트() {
        // Given
        String userId1 = "user1";
        String userId2 = "user2";
        String userId3 = "user3";
        
        String tokenId1 = queueService.issueToken(userId1);
        String tokenId2 = queueService.issueToken(userId2);
        String tokenId3 = queueService.issueToken(userId3);

        // When - 첫 번째 사용자 활성화
        queueService.activateUser(userId1);
        
        // Then
        assertTrue(queueService.isUserActive(userId1));
        assertFalse(queueService.isUserActive(userId2));
        assertFalse(queueService.isUserActive(userId3));
    }

    @Test
    void 대기열_순서_보장_테스트() {
        // Given
        String userId1 = "user1";
        String userId2 = "user2";
        String userId3 = "user3";
        
        String tokenId1 = queueService.issueToken(userId1);
        String tokenId2 = queueService.issueToken(userId2);
        String tokenId3 = queueService.issueToken(userId3);

        // When
        QueueService.QueueStatus status1 = queueService.getQueueStatus(tokenId1);
        QueueService.QueueStatus status2 = queueService.getQueueStatus(tokenId2);
        QueueService.QueueStatus status3 = queueService.getQueueStatus(tokenId3);

        // Then - 순서가 보장되어야 함
        assertTrue(status1.getQueuePosition() < status2.getQueuePosition());
        assertTrue(status2.getQueuePosition() < status3.getQueuePosition());
    }

    @Test
    void 토큰_만료_테스트() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);

        // When - 토큰 만료
        queueService.expireToken(tokenId);

        // Then
        assertThrows(QueueTokenException.ExpiredTokenException.class, () -> {
            queueService.validateToken(tokenId);
        });
    }

    @Test
    void 동시성_대기열_진입_테스트() throws InterruptedException {
        // Given
        int threadCount = 10;
        String[] userIds = new String[threadCount];
        String[] tokenIds = new String[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            userIds[i] = "user" + i;
        }

        // When - 동시에 토큰 발급
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                tokenIds[index] = queueService.issueToken(userIds[index]);
            });
            threads[i].start();
        }

        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - 모든 토큰이 발급되었고 순서가 보장되어야 함
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(tokenIds[i]);
            QueueService.QueueStatus status = queueService.getQueueStatus(tokenIds[i]);
            assertTrue(status.getQueuePosition() > 0);
        }
    }
}
