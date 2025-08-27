package com.grow.member_service.achievement.accomplished.application.model;

import java.time.LocalDateTime;

import com.grow.member_service.common.exception.AccomplishedException;
import com.grow.member_service.global.exception.ErrorCode;

import lombok.Getter;

@Getter
public class AccomplishedPeriod {

	private final LocalDateTime startAt;
	private final LocalDateTime endAt;

	/**
	 * @param startAt 조회 시작일시 (필수)
	 * @param endAt   조회 종료일시 (필수)
	 * @throws AccomplishedException startAt, endAt 중 하나라도 null 이거나 startAt > endAt 인 경우
	 */
	public AccomplishedPeriod(LocalDateTime startAt, LocalDateTime endAt) {
		if (startAt == null || endAt == null) {
			throw new AccomplishedException(ErrorCode.ACCOMPLISHED_PERIOD_INCOMPLETE);
		}
		if (startAt.isAfter(endAt)) {
			throw new AccomplishedException(ErrorCode.ACCOMPLISHED_PERIOD_INVALID_RANGE);
		}
		this.startAt = startAt;
		this.endAt   = endAt;
	}
}