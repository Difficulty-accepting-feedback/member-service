package com.grow.member_service.history.point.infra.persistence.mapper;

import org.springframework.stereotype.Component;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;

@Component
public class PointHistoryMapper {
    /**
     * PointHistoryJpaEntity를 PointHistory 도메인 객체로 변환
     * @param e
     * @return
     */
	public PointHistory toDomain(PointHistoryJpaEntity e) {
		return new PointHistory(
			e.getPointHistoryId(),
            e.getMemberId(),
            e.getAmount(),
            e.getContent(),
            e.getAddAt(),
			e.getActionType(),
            e.getSourceType(),
            e.getSourceId(),
            e.getDedupKey(),
            e.getBalanceAfter()
		);
	}

    /**
     * PointHistory 도메인 객체를 PointHistoryJpaEntity로 변환
     * @param d
     * @return
     */
	public PointHistoryJpaEntity toEntity(PointHistory d) {
		return PointHistoryJpaEntity.builder()
			.memberId(d.getMemberId())
			.amount(d.getAmount())
			.content(d.getContent())
			.addAt(d.getAddAt())
			.actionType(d.getActionType())
			.sourceType(d.getSourceType())
			.sourceId(d.getSourceId())
			.dedupKey(d.getDedupKey())
			.balanceAfter(d.getBalanceAfter())
			.build();
	}
}