package com.grow.member_service.review.domain.model;

import lombok.Getter;

@Getter
public class Review {

    private Long reviewId;
    private Long reviewerId; // 리뷰 작성자 ID
    private Long revieweeId; // 리뷰 대상자 ID
    private String content; // 평가 내용
    private Double totalScore; // 총 점수

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
}
