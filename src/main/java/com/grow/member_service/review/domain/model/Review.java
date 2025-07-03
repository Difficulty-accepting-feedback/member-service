package com.grow.member_service.review.domain.model;

import lombok.Getter;

@Getter
public class Review {

    private final Long reviewId;
    private final Long reviewerId; // 리뷰 작성자 ID
    private final Long revieweeId; // 리뷰 대상자 ID
    private final String content; // 평가 내용
    private final Double totalScore; // 총 점수

    public Review(Long reviewerId,
                  Long revieweeId,
                  String content,
                  Double totalScore
    ) {
        this.reviewId = null;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.content = content;
        this.totalScore = totalScore;
    }

    public Review(Long reviewId,
                  Long reviewerId,
                  Long revieweeId,
                  String content,
                  Double totalScore
    ) {
        this.reviewId = reviewId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.content = content;
        this.totalScore = totalScore;
    }
}
