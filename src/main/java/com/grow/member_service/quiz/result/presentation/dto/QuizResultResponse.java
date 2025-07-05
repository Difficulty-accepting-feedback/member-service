package com.grow.member_service.quiz.result.presentation.dto;

import com.grow.member_service.quiz.result.domain.model.QuizResult;

import lombok.Data;

@Data
public class QuizResultResponse {
	private Long quizResultId;
	private Long memberId;
	private Long quizId;
	private Boolean isCorrect;

	public static QuizResultResponse from (QuizResult quizResult) {
		QuizResultResponse response = new QuizResultResponse();
		response.setQuizResultId(quizResult.getQuizResultId());
		response.setMemberId(quizResult.getMemberId());
		response.setQuizId(quizResult.getQuizId());
		response.setIsCorrect(quizResult.getIsCorrect());
		return response;
	}
}
