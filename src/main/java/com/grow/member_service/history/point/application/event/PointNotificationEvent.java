package com.grow.member_service.history.point.application.event;

import java.time.LocalDateTime;

public record PointNotificationEvent(
	Long memberId,
	String code,              //
	String notificationType,  //
	String title,             //
	String content,           // ex) "+100점 • 잔액 1500점 • 사유: 출석 보상"
	LocalDateTime occurredAt
) {}