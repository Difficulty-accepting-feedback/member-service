package com.grow.member_service.achievement.accomplished.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grow.member_service.achievement.accomplished.domain.model.Accomplished;

public interface AccomplishedRepository {
    Accomplished save(Accomplished domain);
    Optional<Accomplished> findById(Long id);
    List<Accomplished> findAllByMemberId(Long memberId);
    List<Accomplished> findAll();
    Page<Accomplished> findByMemberId(Long memberId, Pageable pageable);
    Page<Accomplished> findByMemberIdAndAccomplishedAtBetween(
        Long memberId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Pageable pageable
    );
    Optional<Accomplished> findByMemberIdAndChallengeId(Long memberId, Long challengeId);

    // 해당 챌린지를 해당 멤버가 달성했는지 여부
    boolean existsByMemberIdAndChallengeId(Long memberId, Long challengeId);
}