package com.grow.member_service.review.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewTest {

	private static final Long REVIEWER_ID = 10L;
	private static final Long REVIEWEE_ID = 20L;
	private static final String CONTENT = "Great work!";

	@Test
	@DisplayName("ID 미지정 생성자 사용 시 reviewId가 null이고 평균 점수를 올바르게 계산한다")
	void constructor_NoId_ComputesAverage() {
		// given
		int s1 = 5, s2 = 4, s3 = 5;

		// when
		Review review = new Review(REVIEWER_ID, REVIEWEE_ID, CONTENT, s1, s2, s3);

		// then
		assertThat(review.getReviewId()).isNull();
		assertThat(review.getReviewerId()).isEqualTo(REVIEWER_ID);
		assertThat(review.getRevieweeId()).isEqualTo(REVIEWEE_ID);
		assertThat(review.getContent()).isEqualTo(CONTENT);
		assertThat(review.getSincerityScore()).isEqualTo(s1);
		assertThat(review.getEnthusiasmScore()).isEqualTo(s2);
		assertThat(review.getCommunicationScore()).isEqualTo(s3);
		// 평균 = (5 + 4 + 5) / 3.0 = 4.666...
		assertThat(review.getTotalScore())
			.isCloseTo(4.6666667, within(1e-6));
	}

	@Test
	@DisplayName("ID 지정 생성자 사용 시 reviewId를 보존하고 평균 점수를 올바르게 계산한다")
	void constructor_WithId_ComputesAverage() {
		// given
		Long reviewId = 99L;
		int s1 = 1, s2 = 2, s3 = 3;

		// when
		Review review = new Review(reviewId, REVIEWER_ID, REVIEWEE_ID, CONTENT, s1, s2, s3);

		// then
		assertThat(review.getReviewId()).isEqualTo(reviewId);
		assertThat(review.getReviewerId()).isEqualTo(REVIEWER_ID);
		assertThat(review.getRevieweeId()).isEqualTo(REVIEWEE_ID);
		assertThat(review.getContent()).isEqualTo(CONTENT);
		assertThat(review.getSincerityScore()).isEqualTo(s1);
		assertThat(review.getEnthusiasmScore()).isEqualTo(s2);
		assertThat(review.getCommunicationScore()).isEqualTo(s3);
		// 평균 = (1 + 2 + 3) / 3.0 = 2.0
		assertThat(review.getTotalScore()).isEqualTo(2.0);
	}

	@Test
	@DisplayName("총점 평균 계산 시 소수점 이하 결과도 정확히 계산한다")
	void average_NonIntegerResult() {
		// given
		int s1 = 0, s2 = 0, s3 = 1;

		// when
		Review review = new Review(REVIEWER_ID, REVIEWEE_ID, CONTENT, s1, s2, s3);

		// then
		// 평균 = (0 + 0 + 1) / 3.0 = 0.333...
		assertThat(review.getTotalScore())
			.isCloseTo(0.3333333, within(1e-6));
	}
}