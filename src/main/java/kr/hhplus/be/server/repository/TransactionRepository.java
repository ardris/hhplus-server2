package kr.hhplus.be.server.repository;

import kr.hhplus.be.server.model.Transaction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 거래 내역 인메모리 저장소
 */
public class TransactionRepository {
    
    private final Map<String, Transaction> transactionStore = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userTransactions = new ConcurrentHashMap<>();
    
    /**
     * 거래 내역 저장
     */
    public Transaction save(Transaction transaction) {
        transactionStore.put(transaction.getTransactionId(), transaction);
        
        // 사용자별 거래 내역 인덱스 업데이트
        userTransactions.computeIfAbsent(transaction.getUserId(), k -> new ArrayList<>())
                        .add(transaction.getTransactionId());
        
        return transaction;
    }
    
    /**
     * 거래 ID로 거래 조회
     */
    public Optional<Transaction> findByTransactionId(String transactionId) {
        return Optional.ofNullable(transactionStore.get(transactionId));
    }
    
    /**
     * 사용자의 거래 내역 조회 (최신순)
     */
    public List<Transaction> findByUserId(String userId) {
        List<String> transactionIds = userTransactions.getOrDefault(userId, new ArrayList<>());
        List<Transaction> transactions = new ArrayList<>();
        
        for (String transactionId : transactionIds) {
            Transaction transaction = transactionStore.get(transactionId);
            if (transaction != null) {
                transactions.add(transaction);
            }
        }
        
        // 최신순으로 정렬
        transactions.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));
        
        return transactions;
    }
    
    /**
     * 사용자의 최근 거래 내역 조회 (최대 개수 제한)
     */
    public List<Transaction> findRecentByUserId(String userId, int limit) {
        List<Transaction> allTransactions = findByUserId(userId);
        List<Transaction> result = new ArrayList<>();
        int count = 0;
        for (Transaction transaction : allTransactions) {
            if (count >= limit) {
                break;
            }
            result.add(transaction);
            count++;
        }
        return result;
    }
    
    /**
     * 사용자의 특정 타입 거래 내역 조회
     */
    public List<Transaction> findByUserIdAndType(String userId, Transaction.TransactionType type) {
        List<Transaction> allTransactions = findByUserId(userId);
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (transaction.getType() == type) {
                result.add(transaction);
            }
        }
        return result;
    }
    
    /**
     * 모든 거래 내역 조회
     */
    public List<Transaction> findAll() {
        return new ArrayList<>(transactionStore.values());
    }
    
    /**
     * 거래 내역 삭제
     */
    public void deleteByTransactionId(String transactionId) {
        Transaction transaction = transactionStore.remove(transactionId);
        if (transaction != null) {
            List<String> userTransactionIds = userTransactions.get(transaction.getUserId());
            if (userTransactionIds != null) {
                userTransactionIds.remove(transactionId);
            }
        }
    }
}
