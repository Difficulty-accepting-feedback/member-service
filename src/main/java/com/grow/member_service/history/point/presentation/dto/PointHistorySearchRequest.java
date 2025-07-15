package com.grow.member_service.history.point.presentation.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class PointHistorySearchRequest {

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private final LocalDateTime startAt;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private final LocalDateTime endAt;

	@AssertTrue(message = "조회 시작일시는 종료일시보다 이전이거나 같아야 합니다.")
	private boolean isValidPeriod() {
		if (startAt == null || endAt == null) return true;
		return !startAt.isAfter(endAt);
	}
}