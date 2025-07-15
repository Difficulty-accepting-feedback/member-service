package com.grow.member_service.review.domain.model;

import lombok.Getter;

@Getter
public class Review {
    private final Long reviewId;
    private final Long reviewerId;
    private final Long revieweeId;
    private final String content;

    private final int sincerityScore;
    private final int enthusiasmScore;
    private final int communicationScore;
    private final Double totalScore;

    public Review(Long reviewerId, Long revieweeId, String content,
        int sincerityScore, int enthusiasmScore, int communicationScore) {
        this(null, reviewerId, revieweeId, content,
            sincerityScore, enthusiasmScore, communicationScore);
    }

    public Review(Long reviewId, Long reviewerId, Long revieweeId, String content,
        int sincerityScore, int enthusiasmScore, int communicationScore) {
        this.reviewId = reviewId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.content = content;
        this.sincerityScore = sincerityScore;
        this.enthusiasmScore = enthusiasmScore;
        this.communicationScore = communicationScore;
        this.totalScore = calculateAverageScore();
    }

    /** 리뷰 점수의 평균을 계산합니다. */
    private double calculateAverageScore() {
        return (sincerityScore + enthusiasmScore + communicationScore) / 3.0;
    }
}