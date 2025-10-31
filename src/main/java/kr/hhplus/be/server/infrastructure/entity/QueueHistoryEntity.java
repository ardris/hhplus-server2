package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 대기열 히스토리 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "queue_history")
@Getter
@Setter
@NoArgsConstructor
public class QueueHistoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    @Column(name = "token_id", nullable = false)
    private String tokenId;
    
    @Column(name = "action_type", nullable = false)
    private String actionType;
    
    @Column(name = "queue_position")
    private Integer queuePosition;
    
    @Column(name = "total_waiting_users")
    private Integer totalWaitingUsers;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
