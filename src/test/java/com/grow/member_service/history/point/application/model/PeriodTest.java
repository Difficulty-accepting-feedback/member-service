package com.grow.member_service.history.point.application.model;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.global.exception.ErrorCode;

class PeriodTest {

	@Test
	@DisplayName("startAt == endAt 일 때 정상 생성")
	void equalStartAndEnd() {
		LocalDateTime now = LocalDateTime.of(2025, 7, 17, 9, 0);
		Period period = new Period(now, now);

		assertThat(period.getStartAt()).isEqualTo(now);
		assertThat(period.getEndAt()).isEqualTo(now);
	}

	@Test
	@DisplayName("startAt < endAt 일 때 정상 생성")
	void validRange() {
		LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 31, 23, 59);
		Period period = new Period(start, end);

		assertThat(period.getStartAt()).isEqualTo(start);
		assertThat(period.getEndAt()).isEqualTo(end);
	}

	@Test
	@DisplayName("startAt 또는 endAt이 null 이면 POINT_PERIOD_INCOMPLETE 예외")
	void nullInputsThrowIncomplete() {
		LocalDateTime now = LocalDateTime.now();

		PointHistoryException ex1 = assertThrows(
			PointHistoryException.class,
			() -> new Period(null, now)
		);
		assertThat(ex1.getErrorCode()).isEqualTo(ErrorCode.POINT_PERIOD_INCOMPLETE);

		PointHistoryException ex2 = assertThrows(
			PointHistoryException.class,
			() -> new Period(now, null)
		);
		assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.POINT_PERIOD_INCOMPLETE);
	}

	@Test
	@DisplayName("startAt가 endAt 이후면 POINT_PERIOD_INVALID_RANGE 예외")
	void invalidRangeThrows() {
		LocalDateTime later  = LocalDateTime.of(2025, 8, 1, 0, 0);
		LocalDateTime earlier = LocalDateTime.of(2025, 7, 1, 0, 0);

		PointHistoryException ex = assertThrows(
			PointHistoryException.class,
			() -> new Period(later, earlier)
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_PERIOD_INVALID_RANGE);
	}
}