package com.grow.member_service.history.point.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);
    List<PointHistory> findByMemberId(Long id);
    void delete(PointHistory pointHistory);

    // 페이징 조회
    Page<PointHistory> findByMemberId(Long memberId, Pageable pageable);

    // 기간(startAt~endAt) 필터링 + 페이징
    Page<PointHistory> findByMemberIdAndPeriod(
        Long memberId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Pageable pageable
    );

    // 멱등성 키로 중복 체크
    boolean existsByDedupKey(String dedupKey);

    // 멱등성 키로 조회
    Optional<PointHistory> findByDedupKey(String dedupKey);

    // 특정 기간 동안 특정 액션 타입의 포인트 기록 개수 조회
    long countByMemberIdAndActionTypeBetween(Long memberId, PointActionType actionType, LocalDateTime startAt, LocalDateTime endAt);
}