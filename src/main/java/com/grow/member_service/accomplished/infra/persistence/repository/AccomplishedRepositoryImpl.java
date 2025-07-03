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

    private final AccomplishedMapper mapper;
    private final AccomplishedJpaRepository jpaRepository;

    @Override
    public Accomplished save(Accomplished domain) {
        AccomplishedJpaEntity entity = mapper.toEntity(domain);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Accomplished> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Accomplished> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Accomplished> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
