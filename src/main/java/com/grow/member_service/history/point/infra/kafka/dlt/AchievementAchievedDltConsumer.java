package com.grow.member_service.history.point.infra.kafka.dlt;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.grow.member_service.global.slack.SlackErrorSendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementAchievedDltConsumer {

	private final SlackErrorSendService slackErrorSendService;

	@KafkaListener(
		topics = "achievement.achieved.dlt",
		groupId = "point-service-achievement-dlt"
	)
	public void consumeDlt(String message) {
		log.info("[ACHIEVEMENT][DLT] 업적 달성 이벤트 처리 실패 수신: {}", message == null ? "" : message.trim());

		slackErrorSendService.sendError(
			"업적 달성 → 포인트 지급 실패",
			"카테고리: [ACHIEVEMENT → POINT]\n"
				+ "상세: 업적 달성 이벤트 처리 중 포인트 지급에 실패했습니다. (DLT)\n"
				+ "영향: 사용자 보상 포인트 미지급 가능",
			message
		);

		log.info("[ACHIEVEMENT][DLT] 실패 이벤트 처리 완료");
	}
}