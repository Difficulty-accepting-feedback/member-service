package com.grow.member_service.review.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "reviewerId",  nullable = false)
    private Long reviewerId; // 리뷰 작성자 ID

    @Column(name = "revieweeId",  nullable = false)
    private Long revieweeId; // 리뷰 대상자 ID

    @Column(name = "revieweeId")
    private String content; // 평가 내용

    /**
     * 점수에 대한 항목을 전체 다 저장해야 할 필요가 있을지 고민해 보아야 함
     * 피어리뷰처럼 전체 항목을 전부 다 보여 주는 게 더 좋은지,,,?
     */
    @Column(name = "totalScore")
    private Double totalScore; // 총 점수

    @Builder
    public ReviewJpaEntity(Long reviewerId,
                           Long revieweeId,
                           String content,
                           Double totalScore
    ) {
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.content = content;
        this.totalScore = totalScore;
    }
}
