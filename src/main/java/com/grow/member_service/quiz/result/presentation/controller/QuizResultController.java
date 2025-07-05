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
import com.grow.member_service.quiz.result.application.service.QuizResultService;
import com.grow.member_service.quiz.result.presentation.dto.QuizResultRequest;
import com.grow.member_service.quiz.result.presentation.dto.QuizResultResponse;
import com.grow.member_service.quiz.result.presentation.dto.QuizStatsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quizzes")
@Tag(name = "QuizResult", description = "퀴즈 결과 관리 API")
public class QuizResultController {
	private final QuizResultService quizResultService;

	/**
	 * 퀴즈 결과 기록
	 * @param quizId 퀴즈 ID
	 * @param quizResultRequest 퀴즈 결과 요청 데이터
	 */
	@Operation(summary = "퀴즈 결과 기록", description = "특정 퀴즈에 대한 회원의 결과를 기록합니다.")
	@PostMapping("/{quizId}/results")
	public ResponseEntity<RsData<QuizResultResponse>> recordResult(
		@Parameter(description = "퀴즈 ID", required = true)
		@PathVariable Long quizId,
		@Parameter(description = "퀴즈 결과 요청 데이터", required = true)
		@RequestBody QuizResultRequest quizResultRequest
	) {
		QuizResult quizResult = quizResultService.recordResult(
			quizResultRequest.getMemberId(),
			quizId,
			quizResultRequest.getIsCorrect()
		);
		QuizResultResponse quizResultResponse = QuizResultResponse.from(quizResult);
		return ResponseEntity.ok(
			new RsData<>("200", "퀴즈 결과 기록 완료", quizResultResponse)
		);
	}

	/**
	 * 퀴즈 결과 목록 조회
	 * @param memberId 회원 ID
	 */
	@Operation(summary = "퀴즈 결과 목록 조회", description = "회원별 퀴즈 결과 리스트를 조회합니다.")
	@GetMapping("/results")
	public ResponseEntity<RsData<List<QuizResultResponse>>> getResultsForMember(
		@Parameter(description = "회원 ID", required = true)
		@RequestParam Long memberId
	) {
		List<QuizResult> results = quizResultService.getResultsForMember(memberId);
		List<QuizResultResponse> quizResultResponseList = results.stream()
			.map(QuizResultResponse::from)
			.collect(Collectors.toList());
		return ResponseEntity.ok(
			new RsData<>("200", "퀴즈 결과 목록 조회 완료", quizResultResponseList)
		);
	}

	/**
	 * 퀴즈 통계 조회
	 * @param memberId 회원 ID
	 */
	@Operation(summary = "퀴즈 통계 조회", description = "회원의 퀴즈 정답 개수 및 성공률을 조회합니다.")
	@GetMapping("/stats")
	public ResponseEntity<RsData<QuizStatsResponse>> getStats(
		@Parameter(description = "회원 ID", required = true)
		@RequestParam Long memberId
	) {
		long correctCount = quizResultService.countCorrectAnswers(memberId);
		double successRate = quizResultService.getSuccessRate(memberId);
		QuizStatsResponse quizStatsResponse = new QuizStatsResponse(correctCount, successRate);
		return ResponseEntity.ok(
			new RsData<>("200", "퀴즈 통계 조회 완료", quizStatsResponse)
		);
	}
}