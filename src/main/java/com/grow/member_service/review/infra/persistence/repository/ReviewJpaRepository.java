package com.grow.member_service.review.infra.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
    List<ReviewJpaEntity> findByRevieweeId(Long revieweeId);
    Optional<ReviewJpaEntity> findByReviewerIdAndRevieweeId(Long reviewerId, Long revieweeId);
    List<ReviewJpaEntity> findByReviewerId(Long reviewerId);
}