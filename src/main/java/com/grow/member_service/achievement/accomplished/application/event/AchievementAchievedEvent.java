package com.grow.member_service.achievement.accomplished.application.event;

import java.time.LocalDateTime;

public record AchievementAchievedEvent(
	Long accomplishedId,
	Long memberId,
	Long challengeId,
	String challengeName,
	int rewardPoint,
	LocalDateTime occurredAt,
	String dedupKey
) {}