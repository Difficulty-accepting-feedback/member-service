package com.grow.member_service.history.subscription.domain.repository;

import com.grow.member_service.history.subscription.domain.model.SubscriptionHistory;

import java.util.List;

public interface SubscriptionHistoryRepository {
    SubscriptionHistory save(SubscriptionHistory domain);
    List<SubscriptionHistory> findByMemberId(Long memberId);
}
