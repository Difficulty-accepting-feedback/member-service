package com.grow.member_service.history.point.presentation.dto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class PointHistorySearchRequestTest {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validator = Validation
			.buildDefaultValidatorFactory()
			.getValidator();
	}

	@Test
	@DisplayName("startAt, endAt 모두 null 이면 검증 통과")
	void bothNull_shouldBeValid() {
		PointHistorySearchRequest req = PointHistorySearchRequest
			.builder()
			.startAt(null)
			.endAt(null)
			.build();

		Set<ConstraintViolation<PointHistorySearchRequest>> violations =
			validator.validate(req);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("startAt ≤ endAt 인 경우 검증 통과")
	void validPeriod_shouldBeValid() {
		LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 31, 23, 59);

		PointHistorySearchRequest req = PointHistorySearchRequest
			.builder()
			.startAt(start)
			.endAt(end)
			.build();

		Set<ConstraintViolation<PointHistorySearchRequest>> violations =
			validator.validate(req);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("startAt > endAt 인 경우 검증 에러 발생")
	void invalidPeriod_shouldHaveViolation() {
		LocalDateTime start = LocalDateTime.of(2025, 8, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 1, 0, 0);

		PointHistorySearchRequest req = PointHistorySearchRequest
			.builder()
			.startAt(start)
			.endAt(end)
			.build();

		Set<ConstraintViolation<PointHistorySearchRequest>> violations =
			validator.validate(req);

		assertThat(violations)
			.hasSize(1)
			.extracting(ConstraintViolation::getMessage)
			.containsExactly("조회 시작일시는 종료일시보다 이전이거나 같아야 합니다.");
	}
}