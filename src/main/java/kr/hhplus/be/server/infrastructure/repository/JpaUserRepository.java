package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 사용자 JPA Repository
 * 
 * Infrastructure Layer에서 데이터베이스와 직접 소통하는 Repository입니다.
 * JPA를 사용하여 CRUD 작업을 수행합니다.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
    
    
    /**
     * 이메일로 조회
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * 활성 사용자 조회
     */
    @Query("SELECT u FROM UserEntity u WHERE u.isActive = true")
    Optional<UserEntity> findActiveUserByUserId(String userId);

    /**
     * 사용자 ID로 조회
     */
    Optional<UserEntity> findByUserId(String userId);
    
    /**
     * 사용자 잔액 차감 (트랜잭션 안전)
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.balance = u.balance - :amount, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId AND u.balance >= :amount")
    int deductBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);
    
    /**
     * 사용자 잔액 충전
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.balance = u.balance + :amount, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    int addBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}
