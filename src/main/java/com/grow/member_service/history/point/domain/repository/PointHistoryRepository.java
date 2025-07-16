package com.grow.member_service.history.point.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grow.member_service.history.point.domain.model.PointHistory;

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

}