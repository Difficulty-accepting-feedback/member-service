package com.grow.member_service.quiz.result.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.quiz.result.application.service.QuizResultService;
import com.grow.member_service.quiz.result.domain.model.QuizResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/quiz-results")
public class QuizInternalController {

	private final QuizResultService quizResultService;

	/**
	 * 멤버가 CORRECT로 맞춘 퀴즈 ID 목록
	 * 응답: RsData<List<Long>>
	 */
	@GetMapping("/members/{memberId}/correct-ids")
	public RsData<List<Long>> correctIds(@PathVariable Long memberId) {
		List<QuizResult> results = quizResultService.getResultsForMember(memberId);
		List<Long> ids = results.stream()
			.filter(r -> Boolean.TRUE.equals(r.getIsCorrect()))
			.map(QuizResult::getQuizId)
			.collect(Collectors.toList());
		return new RsData<>("200", "정답 퀴즈 ID 조회 완료", ids);
	}

	/**
	 * 멤버의 퀴즈 ID 목록 조회 (필터링 가능)
	 * - categoryId: null이면 전체 카테고리, 값이 있으면 해당 카테고리
	 * - correct: null이면 전체, true면 맞춘 퀴즈, false면 틀린 퀴즈
	 * 응답: RsData<List<Long>>
	 * @param memberId 멤버 ID
	 * @param categoryId 카테고리 ID (optional)
	 * @param correct 정답 여부 (optional)
	 * @return RsData<List<Long>>
	 */
	@GetMapping("/members/{memberId}/ids")
	public RsData<List<Long>> ids(
		@PathVariable Long memberId,
		@RequestParam(required = false) Long categoryId,
		@RequestParam(required = false) Boolean correct
	) {
		List<QuizResult> results = quizResultService.findByMemberWithFilter(memberId, categoryId, correct);

		List<Long> ids = results.stream()
			.map(QuizResult::getQuizId)
			.collect(Collectors.toList());

		return new RsData<>("200", "퀴즈 결과 ID 조회 완료", ids);
	}
}