package com.grow.member_service.review.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.ReviewException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.service.MemberProfileService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.review.application.dto.ReviewCandidateResponse;
import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.domain.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewApplicationServiceImpl implements ReviewApplicationService {

	private final ReviewRepository reviewRepository;
	private final MemberProfileService memberProfileService;
	private final MemberRepository memberRepository;

	/**
	 * 리뷰를 남기는 메소드
	 * @param reviewerId 리뷰어의 ID
	 * @param revieweeId 리뷰 대상자의 ID
	 * @param content 리뷰 내용
	 * @param sincerityScore 성실성 점수
	 * @param enthusiasmScore 적극성 점수
	 * @param communicationScore 커뮤니케이션 점수
	 * @return 저장된 리뷰 객체
	 */
	@Override
	@Transactional
	public Review submitReview(Long reviewerId, Long revieweeId, String content,
		int sincerityScore, int enthusiasmScore, int communicationScore) {

		if (reviewerId.equals(revieweeId)) {
			throw new ReviewException(ErrorCode.REVIEW_SELF_NOT_ALLOWED);
		}

		reviewRepository.findByReviewerIdAndRevieweeId(reviewerId, revieweeId)
			.ifPresent(existing -> {
				throw new ReviewException(ErrorCode.REVIEW_ALREADY_EXISTS);
			});

		Review review = new Review(reviewerId, revieweeId, content,
			sincerityScore, enthusiasmScore, communicationScore);

		Review saved = reviewRepository.save(review);

		double avg = review.getTotalScore();
		double delta = calculateScoreDelta(avg);
		memberProfileService.adjustScore(revieweeId, delta);  // 온도 조정

		return saved;
	}

	/**
	 * 리뷰 점수에 따라 온도 조정값을 계산하는 메소드
	 * @param score 리뷰 점수
	 * @return 온도 조정값
	 */
	private double calculateScoreDelta(double score) {
		if (score >= 4.5) return 0.2;
		if (score >= 4.0) return 0.1;
		if (score >= 3.0) return 0.0;
		if (score >= 2.0) return -0.1;
		return -0.2;
	}

	/**
	 * 리뷰를 조회하는 메소드
	 * @param revieweeId 리뷰 대상자의 ID
	 * @return 리뷰 목록
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Review> getReviews(Long revieweeId) {
		List<Review> reviews = reviewRepository.findByRevieweeId(revieweeId);

		if (reviews.isEmpty()) {
			throw new ReviewException(ErrorCode.REVIEW_NOT_FOUND);
		}

		return reviews;
	}

	/**
	 * 작성 가능한 리뷰 대상자를 조회하는 메소드
	 * @param reviewerId 리뷰어의 ID
	 * @return 리뷰 대상자 목록
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ReviewCandidateResponse> getReviewCandidates(Long reviewerId) {

		List<Long> reviewedIds = reviewRepository.findRevieweeIdsByReviewerId(reviewerId);

		// 전체 멤버 대상 (추후에 스터디 연결 시 수정 필요)
		List<Member> all = memberRepository.findAllExcept(reviewerId);

		// 아직 리뷰하지 않은 대상 필터링
		return all.stream()
			.filter(member -> !reviewedIds.contains(member.getMemberId()))
			.map(ReviewCandidateResponse::from)
			.toList();
	}
}