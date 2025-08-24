package com.grow.member_service.history.point.application.event;

import java.time.LocalDateTime;

import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

public record PointNotificationEvent(
	Long pointHistoryId,
	Long memberId,
	int amount, // 적립은 양수, 차감은 음수
	long balanceAfter, // 거래 후 잔액
	PointActionType actionType,
	SourceType sourceType,
	String sourceId,
	String content,
	LocalDateTime occurredAt,
	String dedupKey         // 멱등키
) {}