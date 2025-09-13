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
		// 필수 값 검증
		if (isCorrect == null) throw new QuizResultException(ErrorCode.NULL_CORRECTNESS);

		// 요청 값 그대로 상태 저장 (정답<->오답 자유롭게 변경)
		repository.upsert(memberId, quizId, isCorrect);

		// 저장된 결과 반환
		return repository.findOne(memberId, quizId)
			.orElseThrow(() -> new QuizResultException(ErrorCode.QUIZ_RESULT_NOT_FOUND));
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

	/**
	 * 특정 멤버의 퀴즈 결과를 카테고리 및 정답 여부로 필터링하여 조회합니다.
	 * @param memberId 멤버 ID
	 * @param categoryId 카테고리 ID (현재 미사용)
	 * @param correct 정답 여부 (null일 경우 필터링 없음)
	 * @return 퀴즈 결과 리스트 (없을 경우 빈 리스트)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<QuizResult> findByMemberWithFilter(Long memberId, Long categoryId, Boolean correct) {
		return repository.findByMemberWithFilter(memberId, correct);
	}
}