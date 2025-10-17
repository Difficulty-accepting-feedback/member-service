package com.grow.member_service.history.point.infra.kafka;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.accomplished.application.event.AchievementAchievedEvent;
import com.grow.member_service.global.util.JsonUtils;
import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementAchievedConsumer {

	private final PointCommandService pointCommandService;

	@KafkaListener(
		topics = "achievement.achieved",
		groupId = "point-service-achievement",
		concurrency = "3"
	)
	@RetryableTopic(
		attempts = "5",
		backoff = @Backoff(delay = 1000, multiplier = 2),
		dltTopicSuffix = ".dlt",
		autoCreateTopics = "true"
	)
	public void onMessage(String payload) {
		try {
			AchievementAchievedEvent ev = JsonUtils.fromJson(payload, AchievementAchievedEvent.class);

			String dedup = (ev.dedupKey() != null && !ev.dedupKey().isBlank())
				? ev.dedupKey()
				: ("ACHV-" + ev.challengeId() + "-MEM-" + ev.memberId());
			LocalDateTime when = (ev.occurredAt() != null ? ev.occurredAt() : LocalDateTime.now());

			// 업적 -> 포인트 지급 트리거만 수행
			pointCommandService.grant(
				ev.memberId(),
				ev.rewardPoint(),
				PointActionType.ACHIEVEMENT,
				SourceType.CHALLENGE,
				String.valueOf(ev.challengeId()),
				"[업적] " + ev.challengeName(),
				dedup,
				when
			);

			log.info("[ACHIEVEMENT][RECEIVE] 업적 이벤트 수신 완료 memberId={} challengeId={} reward={}",
				ev.memberId(), ev.challengeId(), ev.rewardPoint());
		} catch (Exception e) {
			log.error("[ACHIEVEMENT][RECEIVE] 업적 이벤트 처리 실패 payload={}", payload, e);
			throw e;
		}
	}
}