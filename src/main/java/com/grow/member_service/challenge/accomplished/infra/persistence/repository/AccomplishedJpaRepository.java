package com.grow.member_service.challenge.accomplished.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.challenge.accomplished.infra.persistence.entity.AccomplishedJpaEntity;

public interface AccomplishedJpaRepository extends JpaRepository<AccomplishedJpaEntity, Long> {
    Optional<AccomplishedJpaEntity> findByMemberIdAndChallengeId(Long memberId, Long challengeId);
    List<AccomplishedJpaEntity> findAllByMemberId(Long memberId);
    Page<AccomplishedJpaEntity> findByMemberId(Long memberId, Pageable pageable);
    Page<AccomplishedJpaEntity> findByMemberIdAndAccomplishedAtBetween(
        Long memberId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Pageable pageable
    );
}