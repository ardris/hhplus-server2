package kr.hhplus.be.server.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 상세 정보 JPA 엔티티
 * 
 * Infrastructure Layer에서 데이터베이스와 매핑되는 엔티티입니다.
 */
@Entity
@Table(name = "user_detail_info")
@Getter
@Setter
@NoArgsConstructor
public class UserDetailEntity {
    
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
