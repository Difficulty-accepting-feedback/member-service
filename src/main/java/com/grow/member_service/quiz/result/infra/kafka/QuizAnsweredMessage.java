package com.grow.member_service.quiz.result.infra.kafka;

import java.time.LocalDateTime;

public record QuizAnsweredMessage(
	Long memberId,
	Long quizId,
	Long categoryId,
	String level,
	String submitted,
	Boolean correct,
	LocalDateTime occurredAt
) {}