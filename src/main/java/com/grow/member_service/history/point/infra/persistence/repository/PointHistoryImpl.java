package com.grow.member_service.history.point.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;
import com.grow.member_service.history.point.infra.persistence.mapper.PointHistoryMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointHistoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository jpaRepository;
    private final PointHistoryMapper mapper;

    @Override
    public PointHistory save(PointHistory pointHistory) {
        PointHistoryJpaEntity entity = mapper.toEntity(pointHistory);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<PointHistory> findByMemberId(Long id) {
        return jpaRepository.findByMemberId(id)
                .stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PointHistory> findByMemberId(Long memberId, Pageable pageable) {
        return jpaRepository.findByMemberId(memberId, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<PointHistory> findByMemberIdAndPeriod(
        Long memberId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Pageable pageable
    ) {
        return jpaRepository.findByMemberIdAndAddAtBetween(memberId, startAt, endAt, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public void delete(PointHistory pointHistory) {
        jpaRepository.delete(mapper.toEntity(pointHistory));
    }
}