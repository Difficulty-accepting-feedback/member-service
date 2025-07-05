package com.grow.member_service.quiz.result.domain.service;

import java.util.List;

import com.grow.member_service.quiz.result.domain.model.QuizResult;

public interface QuizResultStatisticsService {
	long countCorrect(List<QuizResult> quizResults);
	double successRate(List<QuizResult> quizResults);
}
