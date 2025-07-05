package com.grow.member_service.quiz.result.presentation.dto;

import lombok.Data;

@Data
public class QuizResultRequest {
	private Long memberId;
	private Boolean isCorrect;
}
