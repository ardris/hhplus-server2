package kr.hhplus.be.server.infrastructure.repository;

import kr.hhplus.be.server.infrastructure.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 거래 내역 JPA Repository
 * 
 * Infrastructure Layer에서 데이터베이스와 직접 소통하는 Repository입니다.
 * JPA를 사용하여 CRUD 작업을 수행합니다.
 */
@Repository
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, Long> {
    
    /**
     * 사용자 ID로 거래 내역 조회
     */
    List<TransactionEntity> findByUserId(String userId);
    
    /**
     * 사용자의 최근 거래 내역 조회 (최대 개수 제한)
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<TransactionEntity> findRecentByUserId(@Param("userId") String userId, org.springframework.data.domain.Pageable pageable);
    
    default List<TransactionEntity> findRecentByUserId(String userId, int limit) {
        return findRecentByUserId(userId, org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    /**
     * 거래 타입별 조회
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.transactionType = :transactionType")
    List<TransactionEntity> findByTransactionType(@Param("transactionType") String transactionType);
}
