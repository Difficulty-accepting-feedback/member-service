package com.grow.member_service.history.point.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryJpaEntity, Long> {

    // 특정 회원의 포인트 기록을 조회합니다.
    List<PointHistoryJpaEntity> findByMemberId(Long memberId);

    // 특정 회원의 포인트 기록을 페이징하여 조회합니다.
    Page<PointHistoryJpaEntity> findByMemberId(Long memberId, Pageable pageable);

    // 특정 회원의 포인트 기록을 특정 기간 동안 조회합니다.
    @Query("select e from PointHistoryJpaEntity e " +
        "where e.memberId=:memberId and e.addAt between :start and :end")
    Page<PointHistoryJpaEntity> findByMemberIdAndAddAtBetween(@Param("memberId") Long memberId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable);

    // 멱등 키로 포인트 기록을 조회합니다.
    Optional<PointHistoryJpaEntity> findByDedupKey(String dedupKey);

    // 특정 회원의 포인트 기록을 특정 액션 타입으로 조회합니다.
    @Query("select count(e) from PointHistoryJpaEntity e " +
        "where e.memberId=:memberId and e.actionType=:actionType and e.addAt between :start and :end")
    long countByMemberIdAndActionTypeBetween(@Param("memberId") Long memberId,
        @Param("actionType") PointActionType actionType,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}