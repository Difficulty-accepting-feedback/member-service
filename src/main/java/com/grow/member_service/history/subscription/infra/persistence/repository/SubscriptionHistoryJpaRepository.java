package com.grow.member_service.history.subscription.infra.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionHistoryJpaEntity;

public interface SubscriptionHistoryJpaRepository extends JpaRepository<SubscriptionHistoryJpaEntity, Long> {
	List<SubscriptionHistoryJpaEntity> findAllByMemberId(Long memberId);
}