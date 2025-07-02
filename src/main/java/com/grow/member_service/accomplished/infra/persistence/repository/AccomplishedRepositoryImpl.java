package com.grow.member_service.accomplished.infra.persistence.repository;

import com.grow.member_service.accomplished.domain.model.Accomplished;
import com.grow.member_service.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.accomplished.infra.persistence.entity.AccomplishedJpaEntity;
import com.grow.member_service.accomplished.infra.persistence.mapper.AccomplishedMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AccomplishedRepositoryImpl implements AccomplishedRepository {

    private final AccomplishedMapper accomplishedMapper;
    private final AccomplishedJpaRepository accomplishedJpaRepository;

    @Override
    public Accomplished save(Accomplished domain) {
        AccomplishedJpaEntity entity = accomplishedMapper.toEntity(domain);
        return accomplishedMapper.toDomain(accomplishedJpaRepository.save(entity));
    }

    @Override
    public Optional<Accomplished> findById(Long id) {
        return accomplishedJpaRepository.findById(id)
                .map(accomplishedMapper::toDomain);
    }

    @Override
    public Optional<Accomplished> findByMemberId(Long memberId) {
        return accomplishedJpaRepository.findByMemberId(memberId)
                .map(accomplishedMapper::toDomain);
    }

    @Override
    public List<Accomplished> findAll() {
        return accomplishedJpaRepository.findAll()
                .stream()
                .map(accomplishedMapper::toDomain)
                .collect(Collectors.toList());
    }
}
