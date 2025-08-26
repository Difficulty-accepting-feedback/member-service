package com.grow.member_service.accomplished.application.model;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.achievement.accomplished.application.model.AccomplishedPeriod;
import com.grow.member_service.common.exception.AccomplishedException;
import com.grow.member_service.global.exception.ErrorCode;

class AccomplishedPeriodTest {

	@Test
	@DisplayName("startAt == endAt 일 때 정상 생성")
	void equalStartAndEnd() {
		LocalDateTime now = LocalDateTime.now();
		AccomplishedPeriod period = new AccomplishedPeriod(now, now);

		assertThat(period.getStartAt()).isEqualTo(now);
		assertThat(period.getEndAt()).isEqualTo(now);
	}

	@Test
	@DisplayName("startAt < endAt 일 때 정상 생성")
	void validRange() {
		LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 31, 23, 59);
		AccomplishedPeriod period = new AccomplishedPeriod(start, end);

		assertThat(period.getStartAt()).isEqualTo(start);
		assertThat(period.getEndAt()).isEqualTo(end);
	}

	@Test
	@DisplayName("startAt or endAt 가 null 이면 AccomplishedException(INCOMPLETE) 발생")
	void nullInputsThrowIncomplete() {
		LocalDateTime now = LocalDateTime.now();

		AccomplishedException ex1 = assertThrows(AccomplishedException.class,
			() -> new AccomplishedPeriod(null, now));
		assertThat(ex1.getErrorCode()).isEqualTo(ErrorCode.ACCOMPLISHED_PERIOD_INCOMPLETE);

		AccomplishedException ex2 = assertThrows(AccomplishedException.class,
			() -> new AccomplishedPeriod(now, null));
		assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.ACCOMPLISHED_PERIOD_INCOMPLETE);
	}

	@Test
	@DisplayName("startAt > endAt 이면 AccomplishedException(INVALID_RANGE) 발생")
	void invalidRangeThrows() {
		LocalDateTime later  = LocalDateTime.of(2025, 8, 1, 0, 0);
		LocalDateTime earlier = LocalDateTime.of(2025, 7, 1, 0, 0);

		AccomplishedException ex = assertThrows(AccomplishedException.class,
			() -> new AccomplishedPeriod(later, earlier));
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOMPLISHED_PERIOD_INVALID_RANGE);
	}
}