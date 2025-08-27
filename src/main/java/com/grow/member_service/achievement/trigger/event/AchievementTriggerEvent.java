package com.grow.member_service.achievement.trigger.event;

import java.time.LocalDateTime;

public record AchievementTriggerEvent(
	Long memberId,
	Long challengeId,
	LocalDateTime occurredAt
) {}