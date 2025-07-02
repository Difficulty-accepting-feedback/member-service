package com.grow.member_service.history.point.infra.persistence.mapper;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PointHistoryMapper {

    // 엔티티를 도메인으로 변환
    public PointHistory toDomain(PointHistoryJpaEntity entity) {
        return new PointHistory(
                entity.getMemberId(),
                entity.getAmount(),
                entity.getContent()
        );
    }

    // 도메인을 엔티티로 변환
    public PointHistoryJpaEntity toEntity(PointHistory pointHistory) {
        return PointHistoryJpaEntity.builder()
                .memberId(pointHistory.getMemberId())
                .amount(pointHistory.getAmount())
                .content(pointHistory.getContent())
                .build();
    }
}
