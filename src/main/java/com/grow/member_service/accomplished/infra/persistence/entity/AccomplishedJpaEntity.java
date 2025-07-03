package com.grow.member_service.accomplished.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "accomplished")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccomplishedJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accomplishedId", nullable = false, updatable = false)
    private Long accomplishedId;

    @Column(name = "memberId",  nullable = false, unique = true, updatable = false)
    private Long memberId; // 멤버 ID

    @Column(name = "memberId",  nullable = false, updatable = false)
    private Long challengeId; // 업적 ID 값

    @Builder
    public AccomplishedJpaEntity(Long memberId,
                                 Long challengeId
    ) {
        this.memberId = memberId;
        this.challengeId = challengeId;
    }
}
