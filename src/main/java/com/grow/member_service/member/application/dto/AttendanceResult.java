package com.grow.member_service.member.application.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record AttendanceResult(
	boolean attended,
	LocalDate day,
	int streak,
	int bestStreak,
	int grantedAmount,
	Long balanceAfter
) {
	/**
	 * 출석 처리 결과를 생성합니다.
	 * @param day 출석한 날짜
	 * @param streak 현재 연속 출석 일수
	 * @param best 최고 연속 출석 일수
	 * @param granted 출석 보상으로 지급된 금액
	 * @param after 출석 처리 후 잔액
	 * @return
	 */
	public static AttendanceResult attended(LocalDate day, int streak, int best, int granted, Long after) {
		return AttendanceResult.builder()
			.attended(true).day(day).streak(streak).bestStreak(best)
			.grantedAmount(granted).balanceAfter(after)
			.build();
	}

	/**
	 * 출석 스킵 결과를 생성합니다.
	 * @param day 출석한 날짜
	 * @param streak 현재 연속 출석 일수
	 * @param best 최고 연속 출석 일수
	 * @return 출석 스킵 결과 객체
	 */
	public static AttendanceResult skipped(LocalDate day, int streak, int best) {
		return AttendanceResult.builder()
			.attended(false).day(day).streak(streak).bestStreak(best)
			.grantedAmount(0).balanceAfter(null)
			.build();
	}
}