package com.grow.member_service.history.point.application.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grow.member_service.history.point.application.dto.PointHistoryResponse;

public interface PointHistoryApplicationService {

	/**
	 * 페이징·정렬 + (옵션)기간 필터링 조회
	 */
	public Page<PointHistoryResponse> searchHistories(
		Long memberId,
		LocalDateTime startAt,
		LocalDateTime endAt,
		Pageable pageable
	);
}