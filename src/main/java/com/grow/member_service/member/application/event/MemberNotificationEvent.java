package com.grow.member_service.member.application.event;

import java.time.LocalDateTime;

public record MemberNotificationEvent(
	Long memberId,
	String code,
	String notificationType,
	String title,
	String content,
	LocalDateTime occurredAt
) {}