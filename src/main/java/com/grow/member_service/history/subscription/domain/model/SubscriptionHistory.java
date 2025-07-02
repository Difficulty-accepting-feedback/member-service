package com.grow.member_service.history.subscription.domain.model;

import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionStatus;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
public class SubscriptionHistory {

    private Long subscriptionHistoryId;
    private Long memberId;
    private SubscriptionStatus subscriptionStatus; // 구독 상태
    private LocalDateTime startAt; // 구독 시작 날짜
    private LocalDateTime endAt; // 구독 만료 날짜
    private LocalDateTime changeAt; // 구독 상태 변경 날짜 (해지 날짜)

    public SubscriptionHistory(Long memberId,
                               Clock startAt) {
        this.subscriptionHistoryId = null;
        this.memberId = memberId;
        this.subscriptionStatus = SubscriptionStatus.ACTIVE;

        if (startAt != null) {
            this.startAt = LocalDateTime.now(startAt);
            this.endAt = LocalDateTime.now(startAt).plusMonths(1);
        }  else {
            this.startAt = LocalDateTime.now();
            this.endAt = LocalDateTime.now().plusMonths(1);
        }
    }
}
