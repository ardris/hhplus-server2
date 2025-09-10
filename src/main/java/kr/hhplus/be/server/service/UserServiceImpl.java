package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.model.User;
import kr.hhplus.be.server.repository.TransactionRepository;
import kr.hhplus.be.server.repository.UserRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 사용자 관리 서비스 구현체
 */
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final QueueService queueService;
    
    // 사용자별 락 (동시성 제어)
    private final java.util.concurrent.ConcurrentHashMap<String, ReentrantLock> userLocks = new java.util.concurrent.ConcurrentHashMap<>();
    
    public UserServiceImpl(UserRepository userRepository, TransactionRepository transactionRepository, QueueService queueService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.queueService = queueService;
    }
    
    @Override
    // 사용자 잔액 충전
    public BigDecimal chargeBalance(String tokenId, BigDecimal amount) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
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
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            user = new User(userId);
        }
            
            // 잔액 충전
            user.chargeBalance(amount);
            userRepository.update(user);
            
            return user.getBalance();
            
        } finally {
            userLock.unlock();
        }
    }
    
    @Override
    // 사용자 잔액 조회
    public BigDecimal getBalance(String tokenId) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            return userOptional.get().getBalance();
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
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        User user = userOptional.get();
            
            // 잔액 차감
            user.deductBalance(amount);
            userRepository.update(user);
            
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
        
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.hasSufficientBalance(amount);
        }
        return false;
    }
    
    @Override
    // 사용자 잔액 충전 (거래 내역 포함)
    public Transaction chargeBalanceWithTransaction(String tokenId, BigDecimal amount) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        User user = userOptional.get();
        BigDecimal previousBalance = user.getBalance();
        
        // 사용자별 락 획득 (동시성 제어)
        ReentrantLock userLock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        userLock.lock();
        
        try {
            // 잔액 충전
            user.chargeBalance(amount);
            userRepository.update(user);
            
            // 거래 내역 생성
            Transaction transaction = new Transaction(
                userId,
                Transaction.TransactionType.CHARGE,
                amount,
                user.getBalance(),
                "잔액 충전"
            );
            transactionRepository.save(transaction);
            
            return transaction;
            
        } finally {
            userLock.unlock();
        }
    }
    
    @Override
    // 사용자의 거래 내역 조회
    public List<Transaction> getUserTransactions(String tokenId, int limit) {
        // 토큰 검증
        var queueToken = queueService.validateToken(tokenId);
        String userId = queueToken.getUserId();
        
        // 사용자 조회
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        return transactionRepository.findRecentByUserId(userId, limit);
    }
}
