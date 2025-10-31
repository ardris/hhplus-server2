package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.infrastructure.repository.JpaUserRepository;
import kr.hhplus.be.server.infrastructure.repository.JpaTransactionRepository;
import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import kr.hhplus.be.server.infrastructure.entity.TransactionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 사용자 관리 서비스 구현체
 */
@Service
public class UserServiceImpl implements UserService {

    private final JpaUserRepository jpaUserRepository;
    private final JpaTransactionRepository jpaTransactionRepository;
    private final QueueService queueService;

    // 사용자별 락 (동시성 제어)
    private final java.util.concurrent.ConcurrentHashMap<String, ReentrantLock> userLocks = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public UserServiceImpl(JpaUserRepository jpaUserRepository, JpaTransactionRepository jpaTransactionRepository,
            QueueService queueService) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaTransactionRepository = jpaTransactionRepository;
        this.queueService = queueService;
    }

    @Override
    // 사용자 잔액 충전
    public BigDecimal chargeBalance(String tokenId, BigDecimal amount) {
        // 토큰 검증
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // 입력값 검증
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        // 사용자 락 획득
        ReentrantLock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());

        userLock.lock();
        try {
            // 사용자 조회 또는 생성
            Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUserId(userId);
            UserEntity userEntity;
            if (userEntityOptional.isEmpty()) {
                userEntity = new UserEntity();
                userEntity.setUserId(userId);
                userEntity.setUsername("사용자" + userId);
                userEntity.setBalance(BigDecimal.ZERO);
                userEntity.setIsActive(true);
                userEntity.setCreatedAt(java.time.LocalDateTime.now());
            } else {
                userEntity = userEntityOptional.get();
            }

            // 잔액 충전
            userEntity.setBalance(userEntity.getBalance().add(amount));
            userEntity.setUpdatedAt(java.time.LocalDateTime.now());
            jpaUserRepository.save(userEntity);

            return userEntity.getBalance();

        } finally {
            userLock.unlock();
        }
    }

    @Override
    // 사용자 잔액 조회
    public BigDecimal getBalance(String tokenId) {
        // 토큰 검증
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // 사용자 조회
        Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUserId(userId);
        if (userEntityOptional.isPresent()) {
            return userEntityOptional.get().getBalance();
        }
        return BigDecimal.ZERO;
    }

    @Override
    // 사용자 잔액 차감
    public void deductBalance(String userId, BigDecimal amount) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }

        // 사용자 락 획득
        ReentrantLock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());

        userLock.lock();
        try {
            // 사용자 조회
            Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUserId(userId);
            if (userEntityOptional.isEmpty()) {
                throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
            }
            UserEntity userEntity = userEntityOptional.get();

            // 잔액 차감
            if (userEntity.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }
            userEntity.setBalance(userEntity.getBalance().subtract(amount));
            userEntity.setUpdatedAt(java.time.LocalDateTime.now());
            jpaUserRepository.save(userEntity);

        } finally {
            userLock.unlock();
        }
    }

    @Override
    // 잔액 충분 여부 확인
    public boolean hasSufficientBalance(String userId, BigDecimal amount) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUserId(userId);
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            return userEntity.getBalance().compareTo(amount) >= 0;
        }
        return false;
    }

    @Override
    // 사용자 잔액 충전 (거래 내역 포함)
    public Transaction chargeBalanceWithTransaction(String tokenId, BigDecimal amount) {
        // 토큰 검증
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // 사용자 조회
        Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUserId(userId);
        if (userEntityOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        UserEntity userEntity = userEntityOptional.get();
        BigDecimal previousBalance = userEntity.getBalance();

        // 사용자별 락 획득 (동시성 제어)
        ReentrantLock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        userLock.lock();

        try {
            // 잔액 충전
            userEntity.setBalance(userEntity.getBalance().add(amount));
            userEntity.setUpdatedAt(java.time.LocalDateTime.now());
            jpaUserRepository.save(userEntity);

            // 거래 내역 생성
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionId("TXN-" + java.util.UUID.randomUUID().toString().substring(0, 8));
            transactionEntity.setUserId(userId);
            transactionEntity.setTransactionType("CHARGE");
            transactionEntity.setAmount(amount);
            transactionEntity.setBalanceAfter(userEntity.getBalance());
            transactionEntity.setDescription("잔액 충전");
            transactionEntity.setCreatedAt(java.time.LocalDateTime.now());
            jpaTransactionRepository.save(transactionEntity);

            return transactionEntity.toDomain();

        } finally {
            userLock.unlock();
        }
    }

    @Override
    // 사용자의 거래 내역 조회
    public List<Transaction> getUserTransactions(String tokenId, int limit) {
        // 토큰 검증
        kr.hhplus.be.server.model.QueueToken queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();

        // 사용자 조회
        Optional<UserEntity> userEntityOptional = jpaUserRepository.findByUserId(userId);
        if (userEntityOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        List<TransactionEntity> transactionEntities = jpaTransactionRepository.findRecentByUserId(userId, limit);
        List<Transaction> transactions = new ArrayList<>();
        for (TransactionEntity entity : transactionEntities) {
            transactions.add(entity.toDomain());
        }
        return transactions;
    }
}
