package com.grow.member_service.quiz.result.application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.QuizResultException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.quiz.result.application.service.QuizResultService;
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
		if (isCorrect == null) {
			throw new QuizResultException(ErrorCode.NULL_CORRECTNESS);
		}
		QuizResult result = new QuizResult(memberId, quizId, isCorrect);
		return repository.save(result);
	}

	@Override
	@Transactional(readOnly = true)
	public List<QuizResult> getResultsForMember(Long memberId) {
		List<QuizResult> list = repository.findByMemberId(memberId);
		if (list.isEmpty()) {
			throw new QuizResultException(ErrorCode.QUIZ_RESULT_NOT_FOUND);
		}
		return list;
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