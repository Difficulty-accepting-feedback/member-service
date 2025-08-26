package com.grow.member_service.achievement.accomplished.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.grow.member_service.achievement.accomplished.domain.model.Accomplished;
import com.grow.member_service.achievement.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.achievement.accomplished.infra.persistence.entity.AccomplishedJpaEntity;
import com.grow.member_service.achievement.accomplished.infra.persistence.mapper.AccomplishedMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccomplishedRepositoryImpl implements AccomplishedRepository {

    private final AccomplishedMapper mapper;
    private final AccomplishedJpaRepository jpaRepository;

    @Override
    public Accomplished save(Accomplished domain) {
        AccomplishedJpaEntity entity = mapper.toEntity(domain);
        AccomplishedJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Accomplished> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Accomplished> findAllByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId, Pageable.unpaged())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Accomplished> findByMemberId(Long memberId, Pageable pageable) {
        return jpaRepository.findByMemberId(memberId, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Accomplished> findByMemberIdAndAccomplishedAtBetween(
        Long memberId, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        return jpaRepository.findByMemberIdAndAccomplishedAtBetween(memberId, startAt, endAt, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public List<Accomplished> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Accomplished> findByMemberIdAndChallengeId(Long memberId, Long challengeId) {
        return jpaRepository.findByMemberIdAndChallengeId(memberId, challengeId)
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsByMemberIdAndChallengeId(Long memberId, Long challengeId) {
        return jpaRepository.existsByMemberIdAndChallengeId(memberId, challengeId);
    }
}