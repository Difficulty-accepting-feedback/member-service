package com.grow.member_service.accomplished.infra.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "accomplished",
    uniqueConstraints = @UniqueConstraint(columnNames = {"memberId", "challengeId"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccomplishedJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accomplishedId", nullable = false, updatable = false)
    private Long accomplishedId;

    @Column(name = "memberId",  nullable = false, updatable = false)
    private Long memberId; // 멤버 ID

    @Column(name = "challengeId",  nullable = false, updatable = false)
    private Long challengeId; // 업적 ID 값

    @Column(name = "accomplishedAt", nullable = false, updatable = false)
    private LocalDateTime accomplishedAt;

    @Builder
    public AccomplishedJpaEntity(Long memberId,
        Long challengeId,
        LocalDateTime accomplishedAt) {
        this.memberId = memberId;
        this.challengeId = challengeId;
        this.accomplishedAt = accomplishedAt;
    }
}