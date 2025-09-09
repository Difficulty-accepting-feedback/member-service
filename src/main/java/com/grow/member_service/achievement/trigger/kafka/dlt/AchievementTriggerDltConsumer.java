package com.grow.member_service.achievement.trigger.kafka.dlt;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.grow.member_service.global.slack.SlackErrorSendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementTriggerDltConsumer {

	private final SlackErrorSendService slackErrorSendService;

	@KafkaListener(
		topics = "achievement.trigger.dlt",
		groupId = "member-service-achievement-dlt"
	)
	public void consumeDlt(String message) {
		log.info("[ACHV TRIGGER DLT] 업적 트리거 이벤트 처리 실패 수신: {}", message == null ? "" : message.trim());

		slackErrorSendService.sendError(
			"업적 트리거 처리 실패",
			"카테고리: [ACHIEVEMENT → MEMBER]\n"
				+ "상세: 업적 트리거 이벤트 처리 중 오류가 발생했습니다. (DLT)\n"
				+ "영향: 업적 달성 기록 누락 가능",
			message
		);

		log.info("[ACHV TRIGGER DLT] 실패 이벤트 처리 완료");
	}
}