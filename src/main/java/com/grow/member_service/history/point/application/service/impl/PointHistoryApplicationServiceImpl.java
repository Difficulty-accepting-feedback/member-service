package com.grow.member_service.history.point.application.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.history.point.application.dto.PointHistoryResponse;
import com.grow.member_service.history.point.application.model.Period;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointHistoryApplicationServiceImpl implements PointHistoryApplicationService {

	private final PointHistoryRepository repository;

	/**
	 * 페이징·정렬 + (옵션)기간 필터링 조회
	 */
	@Override
	public Page<PointHistoryResponse> searchHistories(
		Long memberId,
		LocalDateTime startAt,
		LocalDateTime endAt,
		Pageable pageable
	) {
		Page<PointHistory> domainPage;

		if (startAt != null && endAt != null) {
			Period period = new Period(startAt, endAt);

			domainPage = repository.findByMemberIdAndPeriod(
				memberId,
				period.getStartAt(),
				period.getEndAt(),
				pageable
			);

			if (domainPage.isEmpty()) {
				throw new PointHistoryException(ErrorCode.POINT_PERIOD_EMPTY);
			}

		} else {
			domainPage = repository.findByMemberId(memberId, pageable);
		}

		return domainPage.map(PointHistoryResponse::fromDomain);
	}
}