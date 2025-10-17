package com.grow.member_service.quiz.result.infra.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.grow.member_service.global.util.JsonUtils;
import com.grow.member_service.quiz.result.application.service.QuizResultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizAnsweredConsumer {

	private final QuizResultService quizResultService;

	@KafkaListener(
		topics = "member.quiz.answered",
		groupId = "member-service-quiz",
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
			QuizAnsweredMessage ev = JsonUtils.fromJson(payload, QuizAnsweredMessage.class);

			quizResultService.recordResult(
				ev.memberId(),
				ev.quizId(),
				ev.correct()
			);

			log.info("[QUIZ][RECEIVE] memberId={} quizId={} correct={}",
				ev.memberId(), ev.quizId(), ev.correct());
		} catch (Exception e) {
			log.error("[QUIZ][ERROR] payload={}", payload, e);
			throw e; // 리트라이 후 실패 시 DLT로 이동
		}
	}
}