package com.grow.member_service.accomplished.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grow.member_service.accomplished.domain.model.Accomplished;

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
}