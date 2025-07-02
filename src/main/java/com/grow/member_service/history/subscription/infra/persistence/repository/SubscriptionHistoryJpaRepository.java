package com.grow.member_service.history.subscription.infra.persistence.repository;

import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionHistoryJpaRepository extends JpaRepository<SubscriptionHistoryJpaEntity, Long> {
}
