package com.grow.member_service.history.point.application.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class PointHistoryResponse {
	private final Long pointHistoryId;
	private final Integer amount;
	private final String content;
	private final LocalDateTime addAt;

	public PointHistoryResponse(Long pointHistoryId,
		Integer amount,
		String content,
		LocalDateTime addAt) {
		this.pointHistoryId = pointHistoryId;
		this.amount = amount;
		this.content = content;
		this.addAt = addAt;
	}

	public static PointHistoryResponse fromDomain(com.grow.member_service.history.point.domain.model.PointHistory ph) {
		return new PointHistoryResponse(
			ph.getPointHistoryId(),
			ph.getAmount(),
			ph.getContent(),
			ph.getAddAt()
		);
	}
}