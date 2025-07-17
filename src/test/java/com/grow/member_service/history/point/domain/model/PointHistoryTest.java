package com.grow.member_service.history.point.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointHistoryTest {

	private static final Long MEMBER_ID = 42L;
	private static final Integer AMOUNT = 150;
	private static final String CONTENT = "Test bonus";

	@Test
	@DisplayName("Clock.fixed 공급 시 addAt이 정확히 고정된 시각으로 설정되어야 한다")
	void constructor_withFixedClock() {
		// given: 2025-07-17T12:34:56Z 고정
		Instant instant = Instant.parse("2025-07-17T12:34:56Z");
		ZoneId zone = ZoneId.of("Asia/Seoul");
		Clock fixedClock = Clock.fixed(instant, zone);
		LocalDateTime expected = LocalDateTime.ofInstant(instant, zone);

		// when
		PointHistory ph = new PointHistory(MEMBER_ID, AMOUNT, CONTENT, fixedClock);

		// then
		assertThat(ph.getPointHistoryId()).isNull();
		assertThat(ph.getMemberId()).isEqualTo(MEMBER_ID);
		assertThat(ph.getAmount()).isEqualTo(AMOUNT);
		assertThat(ph.getContent()).isEqualTo(CONTENT);
		assertThat(ph.getAddAt()).isEqualTo(expected);
	}

	@Test
	@DisplayName("Clock이 null일 때 addAt이 현재 시각 범위 내에 설정되어야 한다")
	void constructor_withNullClock() {
		// given
		LocalDateTime before = LocalDateTime.now();
		// when
		PointHistory ph = new PointHistory(MEMBER_ID, AMOUNT, CONTENT, null);
		LocalDateTime after = LocalDateTime.now();

		// then
		assertThat(ph.getAddAt())
			.isBetween(before, after);
	}

	@Test
	@DisplayName("명시적 addAt 및 pointHistoryId 파라미터 생성자에서 모든 필드가 그대로 세팅되어야 한다")
	void constructor_withAllArgs() {
		// given
		Long historyId = 999L;
		LocalDateTime at = LocalDateTime.of(2025, 7, 1, 8, 0);

		// when
		PointHistory ph = new PointHistory(historyId, MEMBER_ID, AMOUNT, CONTENT, at);

		// then
		assertThat(ph.getPointHistoryId()).isEqualTo(historyId);
		assertThat(ph.getMemberId()).isEqualTo(MEMBER_ID);
		assertThat(ph.getAmount()).isEqualTo(AMOUNT);
		assertThat(ph.getContent()).isEqualTo(CONTENT);
		assertThat(ph.getAddAt()).isEqualTo(at);
	}
}