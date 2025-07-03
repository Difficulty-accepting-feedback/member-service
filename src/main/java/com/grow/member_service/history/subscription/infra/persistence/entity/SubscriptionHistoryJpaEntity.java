package com.grow.member_service.history.subscription.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "subscriptionHistory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriptionHistoryId", nullable = false, updatable = false)
    private Long subscriptionHistoryId;

    @Column(name = "memberId", nullable = false, updatable = false)
    private Long memberId; //

    @Column(name = "subscriptionStatus", nullable = false, updatable = false)
    private SubscriptionStatus subscriptionStatus; // 구독 상태

    @Column(name = "startAt",  nullable = false, updatable = false)
    private LocalDateTime startAt; // 구독 시작 날짜

    @Column(name = "endAt",  nullable = false, updatable = false)
    private LocalDateTime endAt; // 구독 만료 날짜

    @Column(name = "changeAt")
    private LocalDateTime changeAt; // 구독 상태 변경 날짜 (해지 날짜)

    @Builder
    public SubscriptionHistoryJpaEntity(Long memberId,
                                        LocalDateTime startAt,
                                        LocalDateTime endAt
    ) {
        this.memberId = memberId;
        this.subscriptionStatus = SubscriptionStatus.ACTIVE;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
