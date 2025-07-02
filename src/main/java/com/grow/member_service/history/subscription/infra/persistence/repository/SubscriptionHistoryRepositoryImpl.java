package com.grow.member_service.history.subscription.infra.persistence.repository;

import com.grow.member_service.history.subscription.domain.model.SubscriptionHistory;
import com.grow.member_service.history.subscription.domain.repository.SubscriptionHistoryRepository;
import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionHistoryJpaEntity;
import com.grow.member_service.history.subscription.infra.persistence.mapper.SubscriptionHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubscriptionHistoryRepositoryImpl implements SubscriptionHistoryRepository {

    private final SubscriptionHistoryMapper mapper;
    private final SubscriptionHistoryJpaRepository jpaRepository;

    @Override
    public SubscriptionHistory save(SubscriptionHistory domain) {
        SubscriptionHistoryJpaEntity entity = mapper.toEntity(domain);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<SubscriptionHistory> findByMemberId(Long memberId) {
        return jpaRepository.findById(memberId).map(mapper::toDomain);
    }
}
