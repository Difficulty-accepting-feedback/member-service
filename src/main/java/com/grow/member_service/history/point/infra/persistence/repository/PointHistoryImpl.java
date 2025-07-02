package com.grow.member_service.history.point.infra.persistence.repository;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;
import com.grow.member_service.history.point.infra.persistence.mapper.PointHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

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
    public void delete(PointHistory pointHistory) {
        jpaRepository.delete(mapper.toEntity(pointHistory));
    }
}
