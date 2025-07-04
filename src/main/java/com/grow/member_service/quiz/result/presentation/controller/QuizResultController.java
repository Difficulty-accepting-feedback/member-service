package com.grow.member_service.quiz.result.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.service.QuizResultService;
import com.grow.member_service.quiz.result.presentation.dto.QuizResultRequest;
import com.grow.member_service.quiz.result.presentation.dto.QuizResultResponse;
import com.grow.member_service.quiz.result.presentation.dto.QuizStatsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quizzes")
public class QuizResultController {
	private final QuizResultService quizResultService;

	/**
	 * 퀴즈 결과 기록
	 * @param quizId
	 * @param quizResultRequest
	 * @return
	 */
	@PostMapping("/{quizId}/results")
	public ResponseEntity<RsData<QuizResultResponse>> recordResult(
		@PathVariable Long quizId,
		@RequestBody QuizResultRequest quizResultRequest
	) {
		QuizResult quizResult = quizResultService.recordResult(
			quizResultRequest.getMemberId(),
			quizId,
			quizResultRequest.getIsCorrect()
		);
		QuizResultResponse quizResultResponse = QuizResultResponse.from(quizResult);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"퀴즈 결과 기록 완료",
			quizResultResponse
		));
	}

	/**
	 * 퀴즈 결과 목록 조회
	 * @param memberId
	 * @return
	 */
	@GetMapping("/results")
	public ResponseEntity<RsData<List<QuizResultResponse>>> getResultsForMember(
		@RequestParam Long memberId
	) {
		List<QuizResult> results = quizResultService.getResultsForMember(memberId);
		List<QuizResultResponse> quizResultResponseList = results.stream()
			.map(QuizResultResponse::from)
			.collect(Collectors.toList());
		return ResponseEntity.ok(new RsData<>(
			"200",
			"퀴즈 결과 목록 조회 완료",
			quizResultResponseList
		));
	}

	/**
	 * 퀴즈 통계 조회
	 * @param memberId
	 * @return
	 */
	@GetMapping("/stats")
	public ResponseEntity<RsData<QuizStatsResponse>> getStats(
		@RequestParam Long memberId
	) {
		long correctCount = quizResultService.countCorrectAnswers(memberId);
		double successRate = quizResultService.getSuccessRate(memberId);
		QuizStatsResponse quizStatsResponse = new QuizStatsResponse(correctCount, successRate);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"퀴즈 통계 조회 완료",
			quizStatsResponse
		));
	}
}
