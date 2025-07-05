package com.grow.member_service.quiz.result.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.repository.QuizResultRepository;
import com.grow.member_service.quiz.result.domain.service.QuizResultStatisticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizResultServiceImpl implements QuizResultService {

	private final QuizResultRepository repository;
	private final QuizResultStatisticsService statisticsService;

	@Override
	public QuizResult recordResult(Long memberId, Long quizId, Boolean isCorrect) {
		QuizResult result = new QuizResult(memberId, quizId, isCorrect);
		return repository.save(result);
	}

	@Override
	@Transactional(readOnly = true)
	public List<QuizResult> getResultsForMember(Long memberId) {
		return repository.findByMemberId(memberId);
	}

	@Override
	@Transactional(readOnly = true)
	public long countCorrectAnswers(Long memberId) {
		List<QuizResult> list = getResultsForMember(memberId);
		return statisticsService.countCorrect(list);
	}

	@Override
	@Transactional(readOnly = true)
	public double getSuccessRate(Long memberId) {
		List<QuizResult> list = getResultsForMember(memberId);
		return statisticsService.successRate(list);
	}
}
