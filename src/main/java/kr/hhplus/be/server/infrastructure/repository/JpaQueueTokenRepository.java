package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.QueueTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 대기열 토큰 JPA Repository
 * 
 * Infrastructure Layer에서 데이터베이스와 직접 소통하는 Repository입니다.
 * JPA를 사용하여 CRUD 작업을 수행합니다.
 */
@Repository
public interface JpaQueueTokenRepository extends JpaRepository<QueueTokenEntity, String> {
    
    
    /**
     * 사용자 ID로 최신 토큰 조회
     */
    @Query("SELECT q FROM QueueTokenEntity q WHERE q.userId = :userId ORDER BY q.issuedAt DESC")
    Optional<QueueTokenEntity> findLatestByUserId(@Param("userId") String userId);
    
    /**
     * 사용자의 최신 토큰 조회 (QueueTokenPort용)
     */
    @Query("SELECT q FROM QueueTokenEntity q WHERE q.userId = :userId ORDER BY q.issuedAt DESC")
    Optional<QueueTokenEntity> findLatestTokenByUserId(@Param("userId") String userId);
    
    /**
     * 활성 토큰 목록 조회
     */
    List<QueueTokenEntity> findByIsActiveTrue();
    
    /**
     * 활성 토큰 목록 조회 (QueueTokenPort용)
     */
    @Query("SELECT q FROM QueueTokenEntity q WHERE q.isActive = true")
    List<QueueTokenEntity> findActiveTokens();
    
    /**
     * 사용자 ID와 활성 상태로 토큰 조회
     */
    @Query("SELECT q FROM QueueTokenEntity q WHERE q.userId = :userId AND q.isActive = :isActive")
    List<QueueTokenEntity> findByUserIdAndIsActive(@Param("userId") String userId, @Param("isActive") boolean isActive);
    
    /**
     * 만료된 토큰 목록 조회
     */
    @Query("SELECT q FROM QueueTokenEntity q WHERE q.expiresAt < :now")
    List<QueueTokenEntity> findExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * 토큰 활성화 (트랜잭션 안전)
     */
    @Modifying
    @Query("UPDATE QueueTokenEntity q SET q.isActive = true, q.activatedAt = CURRENT_TIMESTAMP WHERE q.tokenId = :tokenId")
    int activateToken(@Param("tokenId") String tokenId);
    
    /**
     * 토큰 비활성화 (트랜잭션 안전)
     */
    @Modifying
    @Query("UPDATE QueueTokenEntity q SET q.isActive = false WHERE q.tokenId = :tokenId")
    int deactivateToken(@Param("tokenId") String tokenId);
    
    /**
     * 사용자의 모든 토큰 비활성화
     */
    @Modifying
    @Query("UPDATE QueueTokenEntity q SET q.isActive = false WHERE q.userId = :userId")
    int deactivateAllUserTokens(@Param("userId") String userId);
}
