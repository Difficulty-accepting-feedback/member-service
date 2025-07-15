package com.grow.member_service.review.application.service;

import java.util.List;

import com.grow.member_service.review.domain.model.Review;

public interface ReviewApplicationService {
	Review submitReview(Long reviewerId, Long revieweeId, String content,
		int sincerityScore, int enthusiasmScore, int communicationScore);

	List<Review> getReviews(Long revieweeId);
}