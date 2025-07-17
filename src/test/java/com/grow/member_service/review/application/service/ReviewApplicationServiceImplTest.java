package com.grow.member_service.review.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.grow.member_service.common.exception.ReviewException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.service.MemberProfileService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.review.application.dto.ReviewCandidateResponse;
import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.domain.repository.ReviewRepository;

class ReviewApplicationServiceImplTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private MemberProfileService memberProfileService;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private ReviewApplicationServiceImpl service;

	private static final Long REVIEWER_ID = 1L;
	private static final Long REVIEWEE_ID = 2L;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("submitReview(): reviewerId == revieweeId 이면 REVIEW_SELF_NOT_ALLOWED 예외")
	void submitReview_selfNotAllowed() {
		assertThrows(
			ReviewException.class,
			() -> service.submitReview(REVIEWER_ID, REVIEWER_ID, "text", 5, 5, 5),
			"Should throw ReviewException when reviewer and reviewee are same"
		);
	}

	@Test
	@DisplayName("submitReview(): 이미 리뷰가 존재하면 REVIEW_ALREADY_EXISTS 예외")
	void submitReview_alreadyExists() {
		given(reviewRepository.findByReviewerIdAndRevieweeId(REVIEWER_ID, REVIEWEE_ID))
			.willReturn(Optional.of(new Review(REVIEWER_ID, REVIEWEE_ID, "x", 5,5,5)));

		assertThrows(
			ReviewException.class,
			() -> service.submitReview(REVIEWER_ID, REVIEWEE_ID, "text", 5,5,5)
		);
	}

	@Test
	@DisplayName("submitReview(): 정상 저장 후 온도 조정 호출 및 Review 반환")
	void submitReview_successAdjustsScore() {
		String content = "good";
		int s1 = 5, s2 = 4, s3 = 5; // average = 4.6667 => delta 0.2
		Review toSave = new Review(REVIEWER_ID, REVIEWEE_ID, content, s1, s2, s3);
		Review saved = new Review(REVIEWER_ID, REVIEWEE_ID, content, s1, s2, s3);
		// ensure repo.find* returns empty
		given(reviewRepository.findByReviewerIdAndRevieweeId(REVIEWER_ID, REVIEWEE_ID))
			.willReturn(Optional.empty());
		// when saving, return saved instance
		given(reviewRepository.save(any(Review.class))).willReturn(saved);

		Review result = service.submitReview(REVIEWER_ID, REVIEWEE_ID, content, s1, s2, s3);

		assertThat(result).isSameAs(saved);
		// average = (5+4+5)/3 = 4.666... => >=4.5 -> delta 0.2
		then(memberProfileService).should()
			.adjustScore(eq(REVIEWEE_ID), eq(0.2));
	}

	@Test
	@DisplayName("getReviews(): 리뷰 없으면 REVIEW_NOT_FOUND 예외")
	void getReviews_notFound() {
		given(reviewRepository.findByRevieweeId(REVIEWEE_ID))
			.willReturn(List.of());

		ReviewException ex = assertThrows(
			ReviewException.class,
			() -> service.getReviews(REVIEWEE_ID)
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
	}

	@Test
	@DisplayName("getReviews(): 리뷰가 있으면 리스트 반환")
	void getReviews_found() {
		Review r1 = new Review(1L, REVIEWEE_ID, "a", 3,3,3);
		Review r2 = new Review(4L, REVIEWEE_ID, "b", 4,4,4);
		given(reviewRepository.findByRevieweeId(REVIEWEE_ID))
			.willReturn(List.of(r1, r2));

		List<Review> list = service.getReviews(REVIEWEE_ID);

		assertThat(list).containsExactly(r1, r2);
	}

	@Test
	@DisplayName("getReviewCandidates(): 리뷰 대상자 필터링 및 매핑")
	void getReviewCandidates_filtersAndMaps() {
		// 이미 리뷰한 대상 ID 리스트
		given(reviewRepository.findRevieweeIdsByReviewerId(REVIEWER_ID))
			.willReturn(List.of(2L, 4L));
		// 모든 회원 조회 (예: IDs 1,2,3,4,5)
		MemberProfile p = new MemberProfile("u@e", "nick", "img", Platform.GOOGLE, "ext");
		MemberAdditionalInfo a = new MemberAdditionalInfo("010","addr");
		Member m1 = new Member(1L, p, a, LocalDateTime.now(), 0, 36.5);
		Member m2 = new Member(2L, p, a, LocalDateTime.now(), 0, 36.5);
		Member m3 = new Member(3L, p, a, LocalDateTime.now(), 0, 36.5);
		Member m4 = new Member(4L, p, a, LocalDateTime.now(), 0, 36.5);
		Member m5 = new Member(5L, p, a, LocalDateTime.now(), 0, 36.5);
		given(memberRepository.findAllExcept(REVIEWER_ID))
			.willReturn(List.of(m1, m2, m3, m4, m5));

		List<ReviewCandidateResponse> candidates =
			service.getReviewCandidates(REVIEWER_ID);

		// 2 and 4 are excluded
		assertThat(candidates)
			.extracting(ReviewCandidateResponse::getMemberId)
			.containsExactlyInAnyOrder(1L, 3L, 5L);

		// mapping correctness
		assertThat(candidates)
			.allSatisfy(dto -> {
				MemberProfile respProfile = p;
				assertThat(dto.getNickname()).isEqualTo(respProfile.getNickname());
				assertThat(dto.getProfileImage()).isEqualTo(respProfile.getProfileImage());
			});
	}
}