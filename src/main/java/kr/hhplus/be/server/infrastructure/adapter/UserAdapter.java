package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.port.UserPort;
import kr.hhplus.be.server.model.User;
import kr.hhplus.be.server.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 사용자 관련 포트를 구현하는 어댑터입니다.
 * 기존 UserRepository를 사용하여 도메인 레이어와 기존 Repository를 연결합니다.
 */
@Repository
public class UserAdapter implements UserPort {
    private final UserRepository userRepository;

    public UserAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(String userId) {
        // 기존 Repository를 통해 사용자 조회
        return userRepository.findByUserId(userId);
    }

    @Override
    public boolean hasEnoughBalance(String userId, BigDecimal amount) {
        // 기존 Repository를 통해 사용자 조회 후 잔액 확인
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        return user.getBalance().compareTo(amount) >= 0;
    }

    @Override
    public void deductBalance(String userId, BigDecimal amount) {
        // 기존 Repository를 통해 사용자 조회 후 잔액 차감
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        User user = userOptional.get();
        BigDecimal newBalance = user.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        user.setBalance(newBalance);
        userRepository.save(user);
    }
}
