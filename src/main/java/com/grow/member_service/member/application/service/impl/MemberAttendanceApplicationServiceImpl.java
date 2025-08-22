package com.grow.member_service.member.application.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;
import com.grow.member_service.member.application.dto.AttendanceResult;
import com.grow.member_service.member.application.service.MemberAttendanceApplicationService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberAttendanceApplicationServiceImpl implements MemberAttendanceApplicationService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final int DAILY_ATTEND_POINT = 10;
	private static final int STREAK_3_BONUS    = 30;
	private static final int STREAK_7_BONUS    = 70;

	private final MemberRepository memberRepository;
	private final PointCommandService point;

	/**
	 * 출석 체크 및 보상 지급
	 * 출석 체크는 하루에 한 번만 가능하며, 이미 출석한 경우에는 보상을 지급하지 않습니다.
	 * 출석 체크 성공 시:
	 * - 출석일자에 해당하는 날짜를 기준으로 출석 기록을 갱신합니다.
	 * - 출석 연속일수에 따라 기본 출석 포인트를 지급합니다.
	 * - 3일 연속 출석 시 30점 보너스, 7일 연속 출석 시 70점 보너스를 지급합니다.
	 * - 출석 체크 실패 시에는 출석 기록을 갱신하지 않고 false를 반환합니다.
	 */
	@Override
	@Transactional
	public AttendanceResult checkInAndReward(Long memberId, LocalDateTime occurredAt) {
		LocalDate today = (occurredAt != null ? occurredAt.atZone(KST).toLocalDate()
			: LocalDate.now(KST));

		Member m = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		// 도메인: 오늘 이미 출석되어 있으면 false
		boolean attended = m.markAttendance(today);
		if (!attended) {
			return AttendanceResult.skipped(today, m.getAttendanceStreak(), m.getAttendanceBestStreak());
		}

		// 스냅샷 저장(@Version)
		memberRepository.save(m);

		String dayKey = today.toString(); // yyyy-MM-dd

		// 1) 기본 출석 포인트
		PointHistory base = point.grant(
			memberId, DAILY_ATTEND_POINT,
			PointActionType.DAILY_CHECK_IN, SourceType.ATTENDANCE,
			dayKey, "출석 체크",
			"ATTEND:" + memberId + ":" + dayKey,
			occurredAt
		);

		// 2) 연속 보너스
		if (m.getAttendanceStreak() == 3) {
			point.grant(memberId, STREAK_3_BONUS,
				PointActionType.STREAK_3, SourceType.ATTENDANCE,
				dayKey, "출석 3일 연속 보너스",
				"ATTEND_STREAK3:" + memberId + ":" + dayKey,
				occurredAt
			);
		} else if (m.getAttendanceStreak() == 7) {
			point.grant(memberId, STREAK_7_BONUS,
				PointActionType.STREAK_7, SourceType.ATTENDANCE,
				dayKey, "출석 7일 연속 보너스",
				"ATTEND_STREAK7:" + memberId + ":" + dayKey,
				occurredAt
			);
		}

		return AttendanceResult.attended(
			today,
			m.getAttendanceStreak(),
			m.getAttendanceBestStreak(),
			DAILY_ATTEND_POINT,
			base.getBalanceAfter()
		);
	}
}