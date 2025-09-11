package com.grow.member_service.quiz.result.infra.kafka.dlt;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.grow.member_service.global.slack.SlackErrorSendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class QuizAnsweredDltConsumer {

	private final SlackErrorSendService slackErrorSendService;

	@KafkaListener(
		topics = "member.quiz.answered.dlt",
		groupId = "member-service-quiz-dlt"
	)
	public void consumeDlt(String message) {
		log.info("[QUIZ DLT] 퀴즈 정답 이벤트 처리 실패 수신: {}", message == null ? "" : message.trim());

		slackErrorSendService.sendError(
			"퀴즈 정답 기록 실패",
			"카테고리: [QUIZ → RESULT]\n"
				+ "상세: 퀴즈 정답 이벤트 처리에 실패했습니다.\n"
				+ "영향: 사용자 퀴즈 결과 누락 가능",
			message
		);

		log.info("[QUIZ DLT] 실패 이벤트 처리 완료");
	}
}