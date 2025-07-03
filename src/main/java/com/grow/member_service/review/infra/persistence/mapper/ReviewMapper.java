package com.grow.member_service.review.infra.persistence.mapper;

import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    // 엔티티를 도메인으로 변경
    public Review toDomain(ReviewJpaEntity entity) {
        return new Review(
                entity.getReviewId(),
                entity.getReviewerId(),
                entity.getRevieweeId(),
                entity.getContent(),
                entity.getTotalScore());
    }

    // 도메인을 엔티티로 변경
    public ReviewJpaEntity toEntity(Review review) {
        return ReviewJpaEntity.builder()
                .reviewerId(review.getReviewerId())
                .revieweeId(review.getRevieweeId())
                .content(review.getContent())
                .totalScore(review.getTotalScore())
                .build();
    }
}
