package com.grow.member_service.member.infra.persistence.entity;

import com.grow.member_service.member.domain.model.Platform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * JPA 엔티티
 */
@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memberId", nullable = false)
    private Long memberId;

    @Column(name = "email", nullable = false, updatable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profileImage")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, updatable = false)
    private Platform platform;

    @Column(name = "platformId", nullable = false, updatable = false)
    private String platformId;
    
    @Column(name = "createAt", nullable = false, updatable = false)
    private LocalDateTime createAt; // 가입 날짜

    @Column(name = "withdrawalAt")
    private LocalDateTime withdrawalAt; // 탈퇴 날짜

    @Column(name = "totalPoint", nullable = false)
    private Integer totalPoint;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "score",  nullable = false)
    private Double score;

    @Column(name = "address")
    private String address;

    @Builder
    public MemberJpaEntity(String email,
                           String nickname,
                           String profileImage,
                           Platform platform,
                           String platformId,
                           LocalDateTime createAt,
                           String phoneNumber,
                           String address
    ) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.platform = platform;
        this.platformId = platformId;
        this.createAt = createAt;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.totalPoint = 0;
        this.score = 36.5;
    }
}
