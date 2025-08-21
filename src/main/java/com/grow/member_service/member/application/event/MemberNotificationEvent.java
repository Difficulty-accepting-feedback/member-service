package com.grow.member_service.member.application.event;

import java.time.LocalDateTime;

public record MemberNotificationEvent(
	Long memberId,
	String type,
	String code,
	String title,
	String content,
	LocalDateTime occurredAt
) {}