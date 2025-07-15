package com.grow.member_service.review.domain.repository;

import java.util.List;
import java.util.Optional;

import com.grow.member_service.review.domain.model.Review;

public interface ReviewRepository {
    Review save(Review review);
    List<Review> findByRevieweeId(Long revieweeId);
    Optional<Review> findByReviewerIdAndRevieweeId(Long reviewerId, Long revieweeId);
    List<Long> findRevieweeIdsByReviewerId(Long reviewerId);
}