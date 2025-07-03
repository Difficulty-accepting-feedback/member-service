package com.grow.member_service.review.infra.persistence.repository;

import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
    List<ReviewJpaEntity> findByRevieweeId(Long revieweeId);
}
