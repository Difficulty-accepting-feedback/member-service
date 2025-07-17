package com.grow.member_service.review.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;

class ReviewMapperTest {

	private ReviewMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new ReviewMapper();
	}

	@Test
	@DisplayName("toDomain(): JPA 엔티티 → 도메인 매핑")
	void toDomain_shouldMapJPAEntityToDomain() {
		// given
		Long id = 555L;
		Long reviewerId = 10L;
		Long revieweeId = 20L;
		String content = "테스트 내용";
		int s1 = 3, s2 = 4, s3 = 5;
		double expectedAverage = (s1 + s2 + s3) / 3.0;

		ReviewJpaEntity entity = ReviewJpaEntity.builder()
			.reviewerId(reviewerId)
			.revieweeId(revieweeId)
			.content(content)
			.sincerityScore(s1)
			.enthusiasmScore(s2)
			.communicationScore(s3)
			.totalScore(0.0) // domain에서 재계산
			.build();

		ReflectionTestUtils.setField(entity, "reviewId", id);

		// when
		Review domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getReviewId()).isEqualTo(id);
		assertThat(domain.getReviewerId()).isEqualTo(reviewerId);
		assertThat(domain.getRevieweeId()).isEqualTo(revieweeId);
		assertThat(domain.getContent()).isEqualTo(content);
		assertThat(domain.getSincerityScore()).isEqualTo(s1);
		assertThat(domain.getEnthusiasmScore()).isEqualTo(s2);
		assertThat(domain.getCommunicationScore()).isEqualTo(s3);
		assertThat(domain.getTotalScore())
			.isCloseTo(expectedAverage, within(1e-6));
	}

	@Test
	@DisplayName("toEntity(): 도메인 → JPA 엔티티 매핑")
	void toEntity_shouldMapDomainToJPAEntity() {
		// given
		Long domainId = 777L;
		Long reviewerId = 30L;
		Long revieweeId = 40L;
		String content = "다른 테스트";
		int s1 = 2, s2 = 2, s3 = 4;
		Review domain = new Review(
			domainId, reviewerId, revieweeId, content, s1, s2, s3
		);
		double expectedAverage = (s1 + s2 + s3) / 3.0;

		// when
		ReviewJpaEntity entity = mapper.toEntity(domain);

		// then
		assertThat(entity.getReviewId()).isNull();
		assertThat(entity.getReviewerId()).isEqualTo(reviewerId);
		assertThat(entity.getRevieweeId()).isEqualTo(revieweeId);
		assertThat(entity.getContent()).isEqualTo(content);
		assertThat(entity.getSincerityScore()).isEqualTo(s1);
		assertThat(entity.getEnthusiasmScore()).isEqualTo(s2);
		assertThat(entity.getCommunicationScore()).isEqualTo(s3);
		assertThat(entity.getTotalScore())
			.isCloseTo(expectedAverage, within(1e-6));
	}
}