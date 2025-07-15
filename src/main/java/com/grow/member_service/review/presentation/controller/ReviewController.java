package com.grow.member_service.review.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.review.application.dto.ReviewCandidateResponse;
import com.grow.member_service.review.application.service.ReviewApplicationService;
import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.presentation.dto.ReviewSubmitRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {

	private final ReviewApplicationService reviewApplicationService;

	@Operation(summary = "리뷰 작성", description = "특정 멤버에 대한 리뷰를 작성합니다.")
	@PostMapping("/{revieweeId}")
	public ResponseEntity<RsData<Review>> submitReview(
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long reviewerId,
		@PathVariable Long revieweeId,
		@RequestBody ReviewSubmitRequest req
	) {
		Review review = reviewApplicationService.submitReview(
			reviewerId,
			revieweeId,
			req.getContent(),
			req.getSincerityScore(),
			req.getEnthusiasmScore(),
			req.getCommunicationScore()
		);

		return ResponseEntity.ok(
			new RsData<>("200", "리뷰 작성 성공", review)
		);
	}

	@Operation(summary = "내가 받은 리뷰 조회", description = "로그인한 사용자가 받은 리뷰를 조회합니다.")
	@GetMapping("/me")
	public ResponseEntity<RsData<List<Review>>> getMyReviews(
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long memberId
	) {
		List<Review> reviews = reviewApplicationService.getReviews(memberId);
		return ResponseEntity.ok(
			new RsData<>("200", "내가 받은 리뷰 조회 성공", reviews)
		);
	}

	@Operation(summary = "작성 가능한 리뷰 목록 조회", description = "아직 리뷰하지 않은 멤버 목록을 조회합니다.")
	@GetMapping("/candidates")
	public ResponseEntity<RsData<List<ReviewCandidateResponse>>> getReviewCandidates(
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long reviewerId
	) {
		List<ReviewCandidateResponse> candidates = reviewApplicationService.getReviewCandidates(reviewerId);
		return ResponseEntity.ok(new RsData<>("200", "작성 가능한 리뷰 목록 조회 성공", candidates));
	}
}