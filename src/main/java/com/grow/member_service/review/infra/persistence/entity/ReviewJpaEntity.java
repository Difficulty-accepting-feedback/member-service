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

	@Column(name = "reviewerId", nullable = false, updatable = false)
	private Long reviewerId; // 리뷰 작성자 ID

	@Column(name = "revieweeId", nullable = false, updatable = false)
	private Long revieweeId; // 리뷰 대상자 ID

	@Column(name = "content",
		nullable = false,
		updatable = false,
		columnDefinition = "TEXT"
	)
	private String content; // 평가 내용

	@Column(name = "sincerity_score", nullable = false, updatable = false)
	private int sincerityScore; // 성실성 점수

	@Column(name = "enthusiasm_score", nullable = false, updatable = false)
	private int enthusiasmScore; // 적극성 점수

	@Column(name = "communication_score", nullable = false, updatable = false)
	private int communicationScore; // 커뮤니케이션 점수

	@Column(name = "totalScore", nullable = false, updatable = false)
	private Double totalScore; // 총 점수

	@Builder
	public ReviewJpaEntity(
		Long reviewerId,
		Long revieweeId,
		String content,
		int sincerityScore,
		int enthusiasmScore,
		int communicationScore,
		Double totalScore) {
		this.reviewerId = reviewerId;
		this.revieweeId = revieweeId;
		this.content = content;
		this.sincerityScore = sincerityScore;
		this.enthusiasmScore = enthusiasmScore;
		this.communicationScore = communicationScore;
		this.totalScore = totalScore;
	}
}