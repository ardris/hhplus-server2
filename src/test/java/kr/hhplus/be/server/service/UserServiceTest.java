package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.repository.QueueTokenRepository;
import kr.hhplus.be.server.repository.TransactionRepository;
import kr.hhplus.be.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 사용자 서비스 단위 테스트
 */
class UserServiceTest {

    private UserService userService;
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        QueueTokenRepository queueTokenRepository = new QueueTokenRepository();
        UserRepository userRepository = new UserRepository();
        TransactionRepository transactionRepository = new TransactionRepository();
        queueService = new QueueServiceImpl(queueTokenRepository);
        userService = new UserServiceImpl(userRepository, transactionRepository, queueService);
    }

    @Test
    void 잔액_충전_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        BigDecimal chargeAmount = new BigDecimal("50000");

        // When
        BigDecimal newBalance = userService.chargeBalance(tokenId, chargeAmount);

        // Then
        assertEquals(chargeAmount, newBalance);
    }

    @Test
    void 잔액_추가_충전_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        BigDecimal firstCharge = new BigDecimal("30000");
        BigDecimal secondCharge = new BigDecimal("20000");

        userService.chargeBalance(tokenId, firstCharge);

        // When
        BigDecimal newBalance = userService.chargeBalance(tokenId, secondCharge);

        // Then
        assertEquals(new BigDecimal("50000"), newBalance);
    }

    @Test
    void 잔액_조회_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        BigDecimal chargeAmount = new BigDecimal("75000");

        userService.chargeBalance(tokenId, chargeAmount);

        // When
        BigDecimal balance = userService.getBalance(tokenId);

        // Then
        assertEquals(chargeAmount, balance);
    }

    @Test
    void 잔액_없는_사용자_조회_시_0_반환() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);

        // When
        BigDecimal balance = userService.getBalance(tokenId);

        // Then
        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void 잔액_차감_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        BigDecimal chargeAmount = new BigDecimal("100000");
        BigDecimal deductAmount = new BigDecimal("30000");

        userService.chargeBalance(tokenId, chargeAmount);

        // When
        userService.deductBalance(userId, deductAmount);

        // Then
        BigDecimal remainingBalance = userService.getBalance(tokenId);
        assertEquals(new BigDecimal("70000"), remainingBalance);
    }

    @Test
    void 잔액_부족시_차감_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        BigDecimal chargeAmount = new BigDecimal("10000");
        BigDecimal deductAmount = new BigDecimal("20000");

        userService.chargeBalance(tokenId, chargeAmount);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.deductBalance(userId, deductAmount);
        });
    }

    @Test
    void 잔액_충분성_확인_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        BigDecimal chargeAmount = new BigDecimal("50000");

        userService.chargeBalance(tokenId, chargeAmount);

        // When & Then
        assertTrue(userService.hasSufficientBalance(userId, new BigDecimal("30000")));
        assertTrue(userService.hasSufficientBalance(userId, new BigDecimal("50000")));
        assertFalse(userService.hasSufficientBalance(userId, new BigDecimal("60000")));
    }

    @Test
    void 잘못된_입력값으로_충전_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.chargeBalance(tokenId, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            userService.chargeBalance(tokenId, BigDecimal.ZERO);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            userService.chargeBalance(tokenId, new BigDecimal("-1000"));
        });
    }

    @Test
    void 잘못된_입력값으로_차감_실패() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        userService.chargeBalance(tokenId, new BigDecimal("50000"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.deductBalance(userId, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            userService.deductBalance(userId, BigDecimal.ZERO);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            userService.deductBalance(userId, new BigDecimal("-1000"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            userService.deductBalance(null, new BigDecimal("1000"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            userService.deductBalance("", new BigDecimal("1000"));
        });
    }

    @Test
    void 거래_내역_포함_잔액_충전_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        BigDecimal chargeAmount = new BigDecimal("50000");

        // When
        Transaction transaction = userService.chargeBalanceWithTransaction(tokenId, chargeAmount);

        // Then
        assertNotNull(transaction);
        assertEquals(userId, transaction.getUserId());
        assertEquals(Transaction.TransactionType.CHARGE, transaction.getType());
        assertEquals(chargeAmount, transaction.getAmount());
        assertEquals(chargeAmount, transaction.getBalanceAfter());
        assertEquals("잔액 충전", transaction.getDescription());
    }

    @Test
    void 사용자_거래_내역_조회_성공() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        
        userService.chargeBalanceWithTransaction(tokenId, new BigDecimal("50000"));
        userService.chargeBalanceWithTransaction(tokenId, new BigDecimal("30000"));

        // When
        List<Transaction> transactions = userService.getUserTransactions(tokenId, 10);

        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        
        // 최신순으로 정렬되어 있는지 확인
        assertTrue(transactions.get(0).getCreatedAt().isAfter(transactions.get(1).getCreatedAt()));
        
        // 모든 거래가 CHARGE 타입인지 확인
        for (Transaction transaction : transactions) {
            assertEquals(Transaction.TransactionType.CHARGE, transaction.getType());
            assertEquals(userId, transaction.getUserId());
        }
    }

    @Test
    void 거래_내역_조회_제한_테스트() {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        
        // 5개의 거래 생성
        for (int i = 0; i < 5; i++) {
            userService.chargeBalanceWithTransaction(tokenId, new BigDecimal("10000"));
        }

        // When - 최대 3개만 조회
        List<Transaction> transactions = userService.getUserTransactions(tokenId, 3);

        // Then
        assertNotNull(transactions);
        assertEquals(3, transactions.size());
    }

    @Test
    void 동시성_잔액_충전_테스트() throws InterruptedException {
        // Given
        String userId = "user123";
        String tokenId = queueService.issueToken(userId);
        int threadCount = 5;
        BigDecimal chargeAmount = new BigDecimal("10000");
        
        // When - 동시에 잔액 충전
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                userService.chargeBalanceWithTransaction(tokenId, chargeAmount);
            });
            threads[i].start();
        }

        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - 최종 잔액이 정확해야 함
        BigDecimal finalBalance = userService.getBalance(tokenId);
        assertEquals(chargeAmount.multiply(new BigDecimal(threadCount)), finalBalance);
        
        // 거래 내역도 정확해야 함
        List<Transaction> transactions = userService.getUserTransactions(tokenId, 10);
        assertEquals(threadCount, transactions.size());
    }
}
