package com.grow.member_service.history.point.application.event;

import java.time.LocalDateTime;

public record PointSpendRequest(
	Long memberId,
	Integer amount,          // 양수로 전달 (히스토리는 음수 기록)
	String actionType,
	String sourceType,
	String sourceId,
	String content,
	String dedupKey,
	LocalDateTime occurredAt
) {}