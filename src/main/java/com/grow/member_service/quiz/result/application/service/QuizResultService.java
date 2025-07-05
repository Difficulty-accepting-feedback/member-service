package com.grow.member_service.quiz.result.application.service;

import java.util.List;

import com.grow.member_service.quiz.result.domain.model.QuizResult;

public interface QuizResultService {

	QuizResult recordResult(Long memberId, Long quizId, Boolean isCorrect);
	List<QuizResult> getResultsForMember(Long memberId);
	long countCorrectAnswers(Long memberId);
	double getSuccessRate(Long memberId);
}
