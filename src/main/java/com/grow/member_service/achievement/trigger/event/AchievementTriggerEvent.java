package com.grow.member_service.achievement.trigger.event;

import java.time.LocalDateTime;

/**
 * 업적 트리거 이벤트 (Kafka 메시지용)
 * - 업적 발생 조건을 알리는 용도
 * - member_service 외부/내부에서 모두 발행 가능
 */
public record AchievementTriggerEvent(
	Long memberId,
	Long challengeId, // 발행 시점에서 확정
	String eventType,
	String dedupKey,
	LocalDateTime occurredAt
) {}