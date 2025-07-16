package com.grow.member_service.history.point.application.model;

import java.time.LocalDateTime;

import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.global.exception.ErrorCode;

import lombok.Getter;

@Getter
public class Period {

	private final LocalDateTime startAt;
	private final LocalDateTime endAt;

	/**
	 * 조회 기간을 나타내는 Value Object
	 * @param startAt 조회 시작일시
	 * @param endAt   조회 종료일시
	 * @throws IllegalArgumentException startAt이 endAt 이후일 경우
	 */
	public Period(LocalDateTime startAt, LocalDateTime endAt) {
		if (startAt == null || endAt == null) {
			throw new PointHistoryException(ErrorCode.POINT_PERIOD_INCOMPLETE);
		}
		if (startAt.isAfter(endAt)) {
			throw new PointHistoryException(ErrorCode.POINT_PERIOD_INVALID_RANGE);
		}
		this.startAt = startAt;
		this.endAt   = endAt;
	}
}