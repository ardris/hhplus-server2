package kr.hhplus.be.server.infrastructure.adapter;

import kr.hhplus.be.server.domain.port.QueueTokenPort;
import kr.hhplus.be.server.infrastructure.entity.QueueTokenEntity;
import kr.hhplus.be.server.infrastructure.repository.JpaQueueTokenRepository;
import kr.hhplus.be.server.model.QueueToken;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 대기열 토큰 데이터 저장/조회 어댑터
 * 
 * QueueTokenPort 인터페이스를 구현해서 도메인 레이어와 데이터베이스를 연결해.
 * 클린아키텍처의 인프라스트럭처 레이어에서 포트를 구현하는 역할을 해.
 * JPA Repository를 사용하여 대기열 토큰을 영구 저장합니다.
 */
@Primary
@Repository
@Transactional
public class JpaQueueTokenAdapter implements QueueTokenPort {
    private final JpaQueueTokenRepository jpaQueueTokenRepository;

    /**
     * JPA Repository를 주입받아 사용해.
     * 도메인 레이어는 저장소에 직접 의존하지 않고, 이 어댑터를 통해 간접적으로 접근해.
     */
    public JpaQueueTokenAdapter(JpaQueueTokenRepository jpaQueueTokenRepository) {
        this.jpaQueueTokenRepository = jpaQueueTokenRepository;
    }

    @Override
    public QueueToken save(QueueToken queueToken) {
        // 도메인 엔티티를 JPA 엔티티로 변환하여 저장
        QueueTokenEntity entity = QueueTokenEntity.fromDomain(queueToken);
        QueueTokenEntity savedEntity = jpaQueueTokenRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<QueueToken> findByTokenId(String tokenId) {
        Optional<QueueTokenEntity> entity = jpaQueueTokenRepository.findById(tokenId);
        return entity.map(QueueTokenEntity::toDomain);
    }

    @Override
    public Optional<QueueToken> findLatestTokenByUserId(String userId) {
        Optional<QueueTokenEntity> entity = jpaQueueTokenRepository.findLatestTokenByUserId(userId);
        return entity.map(QueueTokenEntity::toDomain);
    }

    @Override
    public List<QueueToken> findActiveTokens() {
        List<QueueTokenEntity> entities = jpaQueueTokenRepository.findActiveTokens();
        List<QueueToken> tokens = new ArrayList<>();
        for (QueueTokenEntity entity : entities) {
            tokens.add(entity.toDomain());
        }
        return tokens;
    }

    @Override
    public List<QueueToken> findExpiredTokens() {
        List<QueueTokenEntity> entities = jpaQueueTokenRepository.findExpiredTokens(LocalDateTime.now());
        List<QueueToken> tokens = new ArrayList<>();
        for (QueueTokenEntity entity : entities) {
            tokens.add(entity.toDomain());
        }
        return tokens;
    }

    @Override
    public void updateToken(QueueToken queueToken) {
        Optional<QueueTokenEntity> entityOptional = jpaQueueTokenRepository.findById(queueToken.getTokenId());
        if (entityOptional.isPresent()) {
            QueueTokenEntity entity = entityOptional.get();
            entity.setIsActive(queueToken.isActive());
            entity.setActivatedAt(queueToken.getActivatedAt());
            entity.setUpdatedAt(LocalDateTime.now());
            jpaQueueTokenRepository.save(entity);
        }
    }

    @Override
    public void deleteExpiredTokens() {
        List<QueueTokenEntity> expiredTokens = jpaQueueTokenRepository.findExpiredTokens(LocalDateTime.now());
        jpaQueueTokenRepository.deleteAll(expiredTokens);
    }
}

