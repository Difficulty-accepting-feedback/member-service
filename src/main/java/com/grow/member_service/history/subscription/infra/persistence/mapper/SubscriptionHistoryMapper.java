package com.grow.member_service.history.subscription.infra.persistence.mapper;

import com.grow.member_service.history.subscription.domain.model.SubscriptionHistory;
import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionHistoryJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class SubscriptionHistoryMapper {

    // 엔티티에서 도메인으로 변환
    public SubscriptionHistory toDomain(SubscriptionHistoryJpaEntity entity) {
        return new SubscriptionHistory(
                entity.getMemberId(),
                toClock(entity.getStartAt())
        );
    }

    // 도메인에서 엔티티로 변환
    public SubscriptionHistoryJpaEntity toEntity(SubscriptionHistory domain) {
        return SubscriptionHistoryJpaEntity.builder()
                .memberId(domain.getMemberId())
                .build();
    }

    private Clock toClock(LocalDateTime time) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = time.atZone(zone).toInstant();
        return Clock.fixed(instant, zone);
    }
}
