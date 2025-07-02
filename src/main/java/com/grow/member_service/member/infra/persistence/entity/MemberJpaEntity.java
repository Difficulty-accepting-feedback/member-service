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
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memberId", nullable = false)
    private Long memberId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profileImage")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private Platform platform;

    @Column(name = "platformId", nullable = false)
    private String platformId;
    
    @Column(name = "createAt", nullable = false)
    private LocalDateTime createAt; // 가입 날짜

    @Column(name = "withdrawalAt")
    private LocalDateTime withdrawalAt; // 탈퇴 날짜

    @Column(name = "totalPoint")
    private Integer totalPoint;

    @Column(name = "phoneNumber", nullable = false)
    private String phoneNumber;

    @Column(name = "score")
    private Double score;

    @Column(name = "address")
    private String address;

    @Builder
    public MemberJpaEntity(String email,
                           String nickname,
                           String platformId,
                           String phoneNumber,
                           String address,
                           Platform platform,
                           String profileImage,
                           Clock createAt) {
        this.email = email;
        this.nickname = nickname;
        this.platformId = platformId;
        this.profileImage = profileImage;
        this.phoneNumber = phoneNumber;
        this.platform = platform;
        this.totalPoint = 0;
        this.score = 36.5;

        // 값을 집어넣으면 원하는 값으로, 값을 집어넣지 않으면 시스템 기본 값으로 적용될 수 있도록
        if (createAt != null) this.createAt = LocalDateTime.now(createAt);
        else this.createAt = LocalDateTime.now();

        if (address != null) this.address = address;
        else this.address = "";
    }

}
