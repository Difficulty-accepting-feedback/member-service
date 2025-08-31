package com.grow.member_service.member.application.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;
import com.grow.member_service.member.application.dto.AttendanceResult;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberAttendanceApplicationServiceImplTest {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	@Mock
	MemberRepository memberRepository;

	@Mock
	PointCommandService point;

	@InjectMocks
	MemberAttendanceApplicationServiceImpl service;

	@Test
	@DisplayName("이미 오늘 출석했다면 스킵되고 포인트 지급이 발생하지 않는다")
	void skip_whenAlreadyCheckedInToday() {
		// given
		Long memberId = 1L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 9, 0);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.markAttendance(today)).thenReturn(false); // 이미 출석됨
		when(member.getAttendanceStreak()).thenReturn(5);
		when(member.getAttendanceBestStreak()).thenReturn(7);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		verify(memberRepository, never()).save(any());
		verify(point, never()).grant(any(), anyInt(), any(), any(), any(), any(), any(), any());

		assertThat(result.attended()).isFalse();
		assertThat(result.day()).isEqualTo(today);
		assertThat(result.streak()).isEqualTo(5);
		assertThat(result.bestStreak()).isEqualTo(7);
		assertThat(result.grantedAmount()).isEqualTo(0);
		assertThat(result.balanceAfter()).isNull();
	}

	@Test
	@DisplayName("첫 출석이면 기본 출석 포인트(10점)만 지급된다")
	void grantDaily_whenFirstCheckIn() {
		// given
		Long memberId = 2L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 8, 30);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.markAttendance(today)).thenReturn(true);
		when(member.getAttendanceStreak()).thenReturn(1);
		when(member.getAttendanceBestStreak()).thenReturn(1);

		PointHistory ph = mock(PointHistory.class);
		when(ph.getBalanceAfter()).thenReturn(123L);
		when(point.grant(anyLong(), anyInt(), any(), any(), anyString(), anyString(), anyString(), any()))
			.thenReturn(ph);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		verify(memberRepository).save(member);

		ArgumentCaptor<Long> memberIdCap = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Integer> amountCap = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<PointActionType> actionCap = ArgumentCaptor.forClass(PointActionType.class);
		ArgumentCaptor<SourceType> sourceCap = ArgumentCaptor.forClass(SourceType.class);
		ArgumentCaptor<String> sourceIdCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> contentCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> dedupCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LocalDateTime> whenCap = ArgumentCaptor.forClass(LocalDateTime.class);

		verify(point, times(1)).grant(
			memberIdCap.capture(),
			amountCap.capture(),
			actionCap.capture(),
			sourceCap.capture(),
			sourceIdCap.capture(),
			contentCap.capture(),
			dedupCap.capture(),
			whenCap.capture()
		);

		assertThat(memberIdCap.getValue()).isEqualTo(memberId);
		assertThat(amountCap.getValue()).isEqualTo(10);
		assertThat(actionCap.getValue()).isEqualTo(PointActionType.DAILY_CHECK_IN);
		assertThat(sourceCap.getValue()).isEqualTo(SourceType.ATTENDANCE);
		assertThat(sourceIdCap.getValue()).isEqualTo(today.toString());          // yyyy-MM-dd
		assertThat(contentCap.getValue()).isEqualTo("출석 체크");
		assertThat(dedupCap.getValue()).isEqualTo("ATTEND:" + memberId + ":" + today);
		assertThat(whenCap.getValue()).isEqualTo(occurredAt);

		assertThat(result.attended()).isTrue();
		assertThat(result.day()).isEqualTo(today);
		assertThat(result.streak()).isEqualTo(1);
		assertThat(result.bestStreak()).isEqualTo(1);
		assertThat(result.grantedAmount()).isEqualTo(10);
		assertThat(result.balanceAfter()).isEqualTo(123L);
	}

	@Test
	@DisplayName("연속 3일 달성 시 기본 지급 + STREAK_3 보너스가 지급된다")
	void grantDailyAndStreak3() {
		// given
		Long memberId = 3L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 7, 0);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.markAttendance(today)).thenReturn(true);
		when(member.getAttendanceStreak()).thenReturn(3);
		when(member.getAttendanceBestStreak()).thenReturn(3);

		PointHistory ph = mock(PointHistory.class);
		when(ph.getBalanceAfter()).thenReturn(200L);
		when(point.grant(anyLong(), anyInt(), any(), any(), anyString(), anyString(), anyString(), any()))
			.thenReturn(ph);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		verify(memberRepository).save(member);
		// 기본 + 보너스 총 2회
		verify(point, times(2)).grant(anyLong(), anyInt(), any(), any(), anyString(), anyString(), anyString(), any());

		// 호출 중 하나는 STREAK_3 여야 함
		verify(point).grant(
			eq(memberId), eq(30),
			eq(PointActionType.STREAK_3), eq(SourceType.ATTENDANCE),
			eq(today.toString()), eq("출석 3일 연속 보너스"),
			eq("ATTEND_STREAK3:" + memberId + ":" + today),
			eq(occurredAt)
		);

		assertThat(result.attended()).isTrue();
		assertThat(result.streak()).isEqualTo(3);
		assertThat(result.bestStreak()).isEqualTo(3);
		assertThat(result.grantedAmount()).isEqualTo(10); // 반환엔 기본 지급 금액만 표시
		assertThat(result.balanceAfter()).isEqualTo(200L);
	}

	@Test
	@DisplayName("연속 7일 달성 시 기본 지급 + STREAK_7 보너스가 지급된다")
	void grantDailyAndStreak7() {
		// given
		Long memberId = 4L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 6, 45);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.markAttendance(today)).thenReturn(true);
		when(member.getAttendanceStreak()).thenReturn(7);
		when(member.getAttendanceBestStreak()).thenReturn(7);

		PointHistory ph = mock(PointHistory.class);
		when(ph.getBalanceAfter()).thenReturn(350L);
		when(point.grant(anyLong(), anyInt(), any(), any(), anyString(), anyString(), anyString(), any()))
			.thenReturn(ph);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		verify(memberRepository).save(member);
		verify(point, times(2)).grant(anyLong(), anyInt(), any(), any(), anyString(), anyString(), anyString(), any());

		verify(point).grant(
			eq(memberId), eq(70),
			eq(PointActionType.STREAK_7), eq(SourceType.ATTENDANCE),
			eq(today.toString()), eq("출석 7일 연속 보너스"),
			eq("ATTEND_STREAK7:" + memberId + ":" + today),
			eq(occurredAt)
		);

		assertThat(result.attended()).isTrue();
		assertThat(result.streak()).isEqualTo(7);
		assertThat(result.bestStreak()).isEqualTo(7);
		assertThat(result.grantedAmount()).isEqualTo(10);
		assertThat(result.balanceAfter()).isEqualTo(350L);
	}
}