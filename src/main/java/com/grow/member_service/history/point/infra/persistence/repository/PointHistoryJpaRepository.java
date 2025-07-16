package com.grow.member_service.history.point.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryJpaEntity, Long> {
    List<PointHistoryJpaEntity> findByMemberId(Long memberId);
    Page<PointHistoryJpaEntity> findByMemberId(Long memberId, Pageable pageable);
    Page<PointHistoryJpaEntity> findByMemberIdAndAddAtBetween(
        Long memberId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Pageable pageable
    );
}