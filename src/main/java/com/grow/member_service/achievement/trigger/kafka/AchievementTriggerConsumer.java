package com.grow.member_service.achievement.trigger.kafka;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.achievement.accomplished.application.service.AccomplishedApplicationService;
import com.grow.member_service.achievement.trigger.event.AchievementTriggerEvent;
import com.grow.member_service.global.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementTriggerConsumer {

	private final AccomplishedApplicationService accomplishedService;

	@KafkaListener(
		topics = "achievement.trigger",
		groupId = "member-service-achievement",
		concurrency = "3"
	)
	@RetryableTopic(
		attempts = "5",
		backoff = @Backoff(delay = 1000, multiplier = 2),
		dltTopicSuffix = ".dlt",
		autoCreateTopics = "true"
	)
	public void onMessage(ConsumerRecord<String, String> rec) throws Exception {
		AchievementTriggerEvent ev = JsonUtils.fromJson(rec.value(), AchievementTriggerEvent.class);

		// 시각 보정
		LocalDateTime when = Optional.ofNullable(ev.occurredAt()).orElse(LocalDateTime.now());

		try {
			// 업적 생성(멱등: (memberId, challengeId) UNIQUE)
			var created = accomplishedService.createAccomplishment(
				ev.memberId(),
				new CreateAccomplishedRequest(ev.challengeId())
			);

			log.info("[ACHV][CREATED] memberId={} challengeId={} part={} off={} at={}",
				ev.memberId(), ev.challengeId(), rec.partition(), rec.offset(), when);

		} catch (Exception e) {
			log.error("[ACHV][ERROR] payload={} part={} off={}", rec.value(), rec.partition(), rec.offset(), e);
			throw e;
		}
	}
}