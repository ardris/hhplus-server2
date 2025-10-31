package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.port.UserPort;
import kr.hhplus.be.server.model.User;
import kr.hhplus.be.server.infrastructure.entity.UserEntity;
import kr.hhplus.be.server.infrastructure.repository.JpaUserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 사용자 관련 포트를 구현하는 JPA 어댑터입니다.
 * JpaUserRepository를 사용하여 도메인 레이어와 데이터베이스를 연결합니다.
 */
@Primary
@Repository
@Transactional
public class JpaUserAdapter implements UserPort {
    private final JpaUserRepository jpaUserRepository;

    public JpaUserAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<User> findById(String userId) {
        Optional<UserEntity> entity = jpaUserRepository.findById(userId);
        return entity.map(UserEntity::toDomain);
    }

    @Override
    public boolean hasEnoughBalance(String userId, BigDecimal amount) {
        Optional<UserEntity> entity = jpaUserRepository.findById(userId);
        if (entity.isEmpty()) {
            return false;
        }
        UserEntity userEntity = entity.get();
        return userEntity.getBalance().compareTo(amount) >= 0;
    }

    @Override
    public void deductBalance(String userId, BigDecimal amount) {
        int updatedRows = jpaUserRepository.deductBalance(userId, amount);
        if (updatedRows == 0) {
            throw new IllegalStateException("잔액이 부족하거나 사용자를 찾을 수 없습니다.");
        }
    }
}