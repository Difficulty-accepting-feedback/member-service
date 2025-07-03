package com.grow.member_service.history.point.domain.repository;

import com.grow.member_service.history.point.domain.model.PointHistory;

import java.util.List;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);
    List<PointHistory> findByMemberId(Long id);
    void delete(PointHistory pointHistory);
}
