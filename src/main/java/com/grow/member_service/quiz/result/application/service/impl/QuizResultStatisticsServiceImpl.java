package com.grow.member_service.quiz.result.application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.service.QuizResultStatisticsService;

@Service
public class QuizResultStatisticsServiceImpl implements QuizResultStatisticsService {

	@Override
	public long countCorrect(List<QuizResult> results) {
		return results.stream()
			.filter(QuizResult::getIsCorrect)
			.count();
	}

	@Override
	public double successRate(List<QuizResult> results) {
		if (results.isEmpty()) {
			return 0.0;
		}
		return (double) countCorrect(results) / results.size();
	}
}