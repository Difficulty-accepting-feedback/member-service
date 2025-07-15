package com.grow.member_service.review.infra.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.grow.member_service.review.domain.model.Review;
import com.grow.member_service.review.domain.repository.ReviewRepository;
import com.grow.member_service.review.infra.persistence.entity.ReviewJpaEntity;
import com.grow.member_service.review.infra.persistence.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;

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

    @Override
    public Optional<Review> findByReviewerIdAndRevieweeId(Long reviewerId, Long revieweeId) {
        return jpaRepository.findByReviewerIdAndRevieweeId(reviewerId, revieweeId)
            .map(mapper::toDomain);
    }
}