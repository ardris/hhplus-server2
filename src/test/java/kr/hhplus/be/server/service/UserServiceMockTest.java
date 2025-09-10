package kr.hhplus.be.server.service;

import kr.hhplus.be.server.model.QueueToken;
import kr.hhplus.be.server.model.Transaction;
import kr.hhplus.be.server.model.User;
import kr.hhplus.be.server.repository.QueueTokenRepository;
import kr.hhplus.be.server.repository.TransactionRepository;
import kr.hhplus.be.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService Mock 단위 테스트
 * 의존성을 Mock으로 분리하여 순수한 사용자 비즈니스 로직만 테스트
 */
@ExtendWith(MockitoExtension.class)
class UserServiceMockTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private QueueService queueService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, transactionRepository, queueService);
    }

    @Test
    void 잔액_충전_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        BigDecimal chargeAmount = new BigDecimal("50000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        User mockUser = new User(userId);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // When
        BigDecimal newBalance = userService.chargeBalance(tokenId, chargeAmount);

        // Then
        assertEquals(chargeAmount, newBalance);
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(userRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    void 잔액_추가_충전_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        BigDecimal firstCharge = new BigDecimal("30000");
        BigDecimal secondCharge = new BigDecimal("20000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        User mockUser = new User(userId);
        mockUser.chargeBalance(firstCharge);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // When
        BigDecimal newBalance = userService.chargeBalance(tokenId, secondCharge);

        // Then
        assertEquals(new BigDecimal("50000"), newBalance);
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(userRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    void 잔액_조회_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        BigDecimal expectedBalance = new BigDecimal("75000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        User mockUser = new User(userId);
        mockUser.chargeBalance(expectedBalance);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));

        // When
        BigDecimal balance = userService.getBalance(tokenId);

        // Then
        assertEquals(expectedBalance, balance);
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(userRepository, times(1)).findByUserId(userId);
    }

    @Test
    void 잔액_없는_사용자_조회시_0_반환_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        User mockUser = new User(userId);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));

        // When
        BigDecimal balance = userService.getBalance(tokenId);

        // Then
        assertEquals(BigDecimal.ZERO, balance);
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(userRepository, times(1)).findByUserId(userId);
    }

    @Test
    void 잔액_차감_성공_Mock_테스트() {
        // Given
        String userId = "user123";
        BigDecimal deductAmount = new BigDecimal("30000");
        
        User mockUser = new User(userId);
        mockUser.chargeBalance(new BigDecimal("100000"));
        
        // Mock 설정
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // When
        userService.deductBalance(userId, deductAmount);

        // Then
        assertEquals(new BigDecimal("70000"), mockUser.getBalance());
        
        // Mock 검증
        verify(userRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    void 잔액_부족시_차감_실패_Mock_테스트() {
        // Given
        String userId = "user123";
        BigDecimal deductAmount = new BigDecimal("20000");
        
        User mockUser = new User(userId);
        mockUser.chargeBalance(new BigDecimal("10000"));
        
        // Mock 설정
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.deductBalance(userId, deductAmount);
        });
        
        // Mock 검증: 차감이 실패했으므로 update가 호출되지 않아야 함
        verify(userRepository, times(1)).findByUserId(userId);
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    void 거래_내역_포함_잔액_충전_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        BigDecimal chargeAmount = new BigDecimal("50000");
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        User mockUser = new User(userId);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });

        // When
        Transaction transaction = userService.chargeBalanceWithTransaction(tokenId, chargeAmount);

        // Then
        assertNotNull(transaction);
        assertEquals(userId, transaction.getUserId());
        assertEquals(Transaction.TransactionType.CHARGE, transaction.getType());
        assertEquals(chargeAmount, transaction.getAmount());
        assertEquals(chargeAmount, transaction.getBalanceAfter());
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(userRepository, times(1)).findByUserId(userId);
        verify(userRepository, times(1)).update(any(User.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void 사용자_거래_내역_조회_성공_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        String userId = "user123";
        int limit = 10;
        
        QueueToken mockToken = new QueueToken(tokenId, userId, 8);
        Transaction transaction1 = new Transaction(userId, Transaction.TransactionType.CHARGE, new BigDecimal("50000"), "잔액 충전");
        Transaction transaction2 = new Transaction(userId, Transaction.TransactionType.CHARGE, new BigDecimal("30000"), "잔액 충전");
        List<Transaction> mockTransactions = Arrays.asList(transaction1, transaction2);
        
        // Mock 설정
        when(queueService.validateToken(tokenId)).thenReturn(mockToken);
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, limit)).thenReturn(mockTransactions);

        // When
        List<Transaction> transactions = userService.getUserTransactions(tokenId, limit);

        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        
        // Mock 검증
        verify(queueService, times(1)).validateToken(tokenId);
        verify(transactionRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId, limit);
    }

    @Test
    void 잘못된_입력값_충전_실패_Mock_테스트() {
        // Given
        String tokenId = "token-123";
        
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
        
        // Mock 검증: 잘못된 입력으로는 저장소가 호출되지 않아야 함
        verify(queueService, never()).validateToken(anyString());
        verify(userRepository, never()).findByUserId(anyString());
        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    void 잘못된_입력값_차감_실패_Mock_테스트() {
        // Given
        String userId = "user123";
        
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
        
        // Mock 검증: 잘못된 입력으로는 저장소가 호출되지 않아야 함
        verify(userRepository, never()).findByUserId(anyString());
        verify(userRepository, never()).update(any(User.class));
    }
}
