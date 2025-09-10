package kr.hhplus.be.server.service;

import kr.hhplus.be.server.exception.QueueTokenException;
import kr.hhplus.be.server.model.QueueToken;
import kr.hhplus.be.server.repository.QueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * QueueService Mock 단위 테스트
 * 의존성을 Mock으로 분리하여 순수한 비즈니스 로직만 테스트
 */
@ExtendWith(MockitoExtension.class)
class QueueServiceMockTest {

    @Mock
    private QueueTokenRepository queueTokenRepository;

    private QueueService queueService;

    @BeforeEach
    void setUp() {
        queueService = new QueueServiceImpl(queueTokenRepository);
    }

    @Test
    void 토큰_발급_성공_Mock_테스트() {
        // Given
        String userId = "user123";
        String expectedTokenId = "token-123";
        
        // Mock 설정: 기존 토큰이 없음을 시뮬레이션
        when(queueTokenRepository.findLatestTokenByUserId(userId)).thenReturn(Optional.empty());
        when(queueTokenRepository.save(any(QueueToken.class))).thenAnswer(invocation -> {
            QueueToken token = invocation.getArgument(0);
            return token;
        });

        // When
        String tokenId = queueService.issueToken(userId);

        // Then
        assertNotNull(tokenId);
        assertFalse(tokenId.isEmpty());
        
        // Mock 검증: 저장소에 토큰이 저장되었는지 확인
        verify(queueTokenRepository, times(1)).findLatestTokenByUserId(userId);
        verify(queueTokenRepository, times(1)).save(any(QueueToken.class));
    }

    @Test
    void 기존_토큰_존재시_재발급_방지_Mock_테스트() {
        // Given
        String userId = "user123";
        String existingTokenId = "existing-token-123";
        QueueToken existingToken = new QueueToken(existingTokenId, userId, 8);
        
        // Mock 설정: 기존 토큰이 존재함을 시뮬레이션
        when(queueTokenRepository.findLatestTokenByUserId(userId)).thenReturn(Optional.of(existingToken));

        // When
        String tokenId = queueService.issueToken(userId);

        // Then
        assertEquals(existingTokenId, tokenId);
        
        // Mock 검증: 새로운 토큰이 저장되지 않았는지 확인
        verify(queueTokenRepository, times(1)).findLatestTokenByUserId(userId);
        verify(queueTokenRepository, never()).save(any(QueueToken.class));
    }

    @Test
    void 토큰_검증_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정: 토큰이 존재하고 유효함을 시뮬레이션
        when(queueTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(mockToken));

        // When
        QueueToken validatedToken = queueService.validateToken(tokenId);

        // Then
        assertNotNull(validatedToken);
        assertEquals(tokenId, validatedToken.getTokenId());
        assertEquals(userId, validatedToken.getUserId());
        
        // Mock 검증
        verify(queueTokenRepository, times(1)).findByTokenId(tokenId);
    }

    @Test
    void 존재하지_않는_토큰_검증_실패_Mock_테스트() {
        // Given
        String invalidTokenId = "invalid-token";
        
        // Mock 설정: 토큰이 존재하지 않음을 시뮬레이션
        when(queueTokenRepository.findByTokenId(invalidTokenId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(QueueTokenException.InvalidTokenException.class, () -> {
            queueService.validateToken(invalidTokenId);
        });
        
        // Mock 검증
        verify(queueTokenRepository, times(1)).findByTokenId(invalidTokenId);
    }

    @Test
    void 만료된_토큰_검증_실패_Mock_테스트() {
        // Given
        String tokenId = "expired-token";
        String userId = "user123";
        QueueToken expiredToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정: 토큰이 만료되었음을 시뮬레이션
        when(queueTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(expiredToken));
        
        // 토큰을 강제로 만료 상태로 설정
        expiredToken.deactivate();

        // When & Then
        assertThrows(QueueTokenException.ExpiredTokenException.class, () -> {
            queueService.validateToken(tokenId);
        });
        
        // Mock 검증
        verify(queueTokenRepository, times(1)).findByTokenId(tokenId);
    }

    @Test
    void 대기열_상태_조회_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정
        when(queueTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(mockToken));

        // When
        QueueService.QueueStatus status = queueService.getQueueStatus(tokenId);

        // Then
        assertNotNull(status);
        assertTrue(status.getQueuePosition() > 0);
        assertTrue(status.getTotalWaitingUsers() >= 0);
        assertNotNull(status.getRemainingTimeInSeconds());
        
        // Mock 검증
        verify(queueTokenRepository, times(1)).findByTokenId(tokenId);
    }

    @Test
    void 사용자_활성화_성공_Mock_테스트() {
        // Given
        String userId = "user123";
        String tokenId = "token-123";
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정
        when(queueTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(mockToken));

        // When
        queueService.activateUser(userId);

        // Then
        assertTrue(queueService.isUserActive(userId));
        
        // Mock 검증
        verify(queueTokenRepository, times(1)).findByTokenId(tokenId);
    }

    @Test
    void 토큰_만료_처리_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        
        // Mock 설정
        when(queueTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(mockToken));

        // When
        queueService.expireToken(tokenId);

        // Then
        assertTrue(mockToken.isExpired());
        
        // Mock 검증
        verify(queueTokenRepository, times(1)).findByTokenId(tokenId);
    }

    @Test
    void 잘못된_입력값_토큰_발급_실패_Mock_테스트() {
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
        
        // Mock 검증: 잘못된 입력으로는 저장소가 호출되지 않아야 함
        verify(queueTokenRepository, never()).findLatestTokenByUserId(anyString());
        verify(queueTokenRepository, never()).save(any(QueueToken.class));
    }
}
