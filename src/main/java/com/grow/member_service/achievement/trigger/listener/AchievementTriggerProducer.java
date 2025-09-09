package com.grow.member_service.achievement.trigger.listener;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.trigger.event.AchievementTriggerEvent;
import com.grow.member_service.global.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementTriggerProducer {

	private static final String TOPIC = "achievement.trigger";
	private final KafkaTemplate<String, String> kafkaTemplate;

	/** 트리거 발행 (key = memberId) */
	public void send(AchievementTriggerEvent e) {
		String key = e.memberId().toString();
		String payload = JsonUtils.toJsonString(e);
		kafkaTemplate.send(TOPIC, key, payload);
		log.info("[KAFKA][SENT][ACHV.TRIGGER] topic={} key={} challengeId={} type={}",
			TOPIC, key, e.challengeId(), e.eventType());
	}

	public void send(Long memberId, Long challengeId, String eventType, String dedupKey, java.time.LocalDateTime occurredAt) {
		send(new AchievementTriggerEvent(memberId, challengeId, eventType, dedupKey, occurredAt));
	}
}