package com.grow.member_service.review.infra.persistence.repository;

import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.domain.repository.ReviewRepository;
import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;
import com.grow.member_service.review.infra.persistence.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewMapper mapper;
    private final ReviewJpaRepository jpaRepository;

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = mapper.toEntity(review);
        ReviewJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Review> findByRevieweeId(Long revieweeId) {
        return jpaRepository.findByRevieweeId(revieweeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
