package com.grow.member_service.review.infra.persistence.repository;

import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
}
