package com.grow.member_service.history.point.infra.persistence.repository;

import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryJpaEntity, Long> {
    List<PointHistoryJpaEntity> findByMemberId(Long memberId);
}
