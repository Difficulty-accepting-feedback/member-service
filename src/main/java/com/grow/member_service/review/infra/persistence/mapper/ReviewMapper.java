package com.grow.member_service.review.infra.persistence.mapper;

import org.springframework.stereotype.Component;

import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;

@Component
public class ReviewMapper {

    // 엔티티를 도메인으로 변경
    public Review toDomain(ReviewJpaEntity entity) {
        return new Review(
            entity.getReviewId(),
            entity.getReviewerId(),
            entity.getRevieweeId(),
            entity.getContent(),
            entity.getSincerityScore(),
            entity.getEnthusiasmScore(),
            entity.getCommunicationScore()
        );
    }

    // 도메인을 엔티티로 변경
    public ReviewJpaEntity toEntity(Review review) {
        return ReviewJpaEntity.builder()
            .reviewerId(review.getReviewerId())
            .revieweeId(review.getRevieweeId())
            .content(review.getContent())
            .sincerityScore(review.getSincerityScore())
            .enthusiasmScore(review.getEnthusiasmScore())
            .communicationScore(review.getCommunicationScore())
            .totalScore(review.getTotalScore())
            .build();
    }
}