package com.grow.member_service.review.domain.repository;

import com.grow.member_service.review.domain.model.Review;

import java.util.List;

public interface ReviewRepository {
    Review save(Review review);
    List<Review> findByReviewerId(Long reviewerId);
    List<Review> findByRevieweeId(Long revieweeId);
}
