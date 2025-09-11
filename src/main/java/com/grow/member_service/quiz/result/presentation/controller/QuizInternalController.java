package com.grow.member_service.quiz.result.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}