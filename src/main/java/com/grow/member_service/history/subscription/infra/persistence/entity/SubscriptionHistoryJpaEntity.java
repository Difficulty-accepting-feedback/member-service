package com.grow.member_service.history.subscription.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "subscriptionHistory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriptionHistoryId", nullable = false)
    private Long subscriptionHistoryId;

    @Column(name = "memberId",  nullable = false)
    private Long memberId; //

    @Column(name = "subscriptionStatus",  nullable = false)
    private SubscriptionStatus subscriptionStatus; // 구독 상태

    @Column(name = "startAt",  nullable = false)
    private LocalDateTime startAt; // 구독 시작 날짜

    @Column(name = "endAt",  nullable = false)
    private LocalDateTime endAt; // 구독 만료 날짜

    @Column(name = "changeAt")
    private LocalDateTime changeAt; // 구독 상태 변경 날짜 (해지 날짜)

    @Builder
    public SubscriptionHistoryJpaEntity(Long memberId,
                                        Clock startAt,
                                        Clock changeAt) {
        this.memberId = memberId;
        this.subscriptionStatus = SubscriptionStatus.ACTIVE;

        if (startAt != null) {
            this.startAt = LocalDateTime.now(startAt);
            this.endAt = LocalDateTime.now(startAt).plusMonths(1); // 한 달 뒤로 자동 만료일 설정
        } else  {
            this.startAt = LocalDateTime.now();
            this.endAt = LocalDateTime.now().plusMonths(1);
        }

        // 변경 날짜를 설정할 경우 설정, 설정하지 않았을 경우에는 null 로 저장
        if (changeAt != null) LocalDateTime.now(changeAt);
        else this.changeAt = null;
    }
}
