package com.grow.member_service.history.subscription.domain.repository;

import com.grow.member_service.history.subscription.domain.model.SubscriptionHistory;

import java.util.Optional;

public interface SubscriptionHistoryRepository {
    Long save(SubscriptionHistory domain);
    Optional<SubscriptionHistory> findByMemberId(Long memberId);
}
