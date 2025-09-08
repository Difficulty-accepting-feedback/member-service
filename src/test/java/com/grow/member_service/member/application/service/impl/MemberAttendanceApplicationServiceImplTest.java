package com.grow.member_service.member.application.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.grow.member_service.member.application.dto.AttendanceResult;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberAttendanceApplicationServiceImplTest {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	@Mock
	MemberRepository memberRepository;

	@Mock
	KafkaTemplate<String, String> kafkaTemplate;

	@InjectMocks
	MemberAttendanceApplicationServiceImpl service;

	private void stubKafkaSend() {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(kafkaTemplate.send(anyString(), anyString(), anyString()))
			.willReturn(CompletableFuture.completedFuture(sendResult));
	}

	@Test
	@DisplayName("이미 오늘 출석했다면 스킵되고 카프카 퍼블리시는 발생하지 않는다")
	void skip_whenAlreadyCheckedInToday() {
		// given
		Long memberId = 1L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 9, 0);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(member.markAttendance(today)).willReturn(false); // 이미 출석됨
		given(member.getAttendanceStreak()).willReturn(5);
		given(member.getAttendanceBestStreak()).willReturn(7);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		then(memberRepository).should(never()).save(any());
		then(kafkaTemplate).should(never()).send(anyString(), anyString(), anyString());

		assertThat(result.attended()).isFalse();
		assertThat(result.day()).isEqualTo(today);
		assertThat(result.streak()).isEqualTo(5);
		assertThat(result.bestStreak()).isEqualTo(7);
		assertThat(result.grantedAmount()).isZero();
		assertThat(result.balanceAfter()).isNull();
	}

	@Test
	@DisplayName("첫 출석이면 기본 출석 포인트(10점) 커맨드가 카프카로 1회 퍼블리시된다")
	void grantDaily_whenFirstCheckIn() {
		// given
		stubKafkaSend();

		Long memberId = 2L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 8, 30);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(member.markAttendance(today)).willReturn(true);
		given(member.getAttendanceStreak()).willReturn(1);
		given(member.getAttendanceBestStreak()).willReturn(1);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		then(memberRepository).should().save(member);

		ArgumentCaptor<String> topicCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCap   = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> payloadCap = ArgumentCaptor.forClass(String.class);

		then(kafkaTemplate).should(times(1)).send(
			topicCap.capture(),
			keyCap.capture(),
			payloadCap.capture()
		);

		String topic = topicCap.getValue();
		String key = keyCap.getValue();
		String payload = payloadCap.getValue();

		// 토픽/키 확인
		assertThat(topic).isEqualTo("point.grant.requested");
		assertThat(key).isEqualTo(memberId.toString());

		// payload는 PointGrantRequest를 JSON 직렬화한 문자열
		// 느슨 검증: 핵심 필드 포함 여부
		assertThat(payload).contains("\"memberId\":" + memberId);
		assertThat(payload).contains("\"amount\":10");
		assertThat(payload).contains("\"actionType\":\"DAILY_CHECK_IN\"");
		assertThat(payload).contains("\"sourceType\":\"ATTENDANCE\"");
		assertThat(payload).contains("\"sourceId\":\"" + today + "\"");
		assertThat(payload).contains("\"content\":\"출석 체크\"");
		assertThat(payload).contains("\"dedupKey\":\"ATTEND:" + memberId + ":" + today + "\"");
		assertThat(payload).contains("\"occurredAt\":\"" + occurredAt); // 로컬데이트타임 toString prefix

		// 반환값(서비스 정책): 기본 지급만 표시, balanceAfter는 null
		assertThat(result.attended()).isTrue();
		assertThat(result.day()).isEqualTo(today);
		assertThat(result.streak()).isEqualTo(1);
		assertThat(result.bestStreak()).isEqualTo(1);
		assertThat(result.grantedAmount()).isEqualTo(10);
		assertThat(result.balanceAfter()).isNull();
	}

	@Test
	@DisplayName("연속 3일 달성 시 기본 + STREAK_3 보너스가 2회 퍼블리시된다")
	void grantDailyAndStreak3() {
		// given
		stubKafkaSend();

		Long memberId = 3L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 7, 0);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(member.markAttendance(today)).willReturn(true);
		given(member.getAttendanceStreak()).willReturn(3);
		given(member.getAttendanceBestStreak()).willReturn(3);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		then(memberRepository).should().save(member);

		ArgumentCaptor<String> topicCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCap   = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> payloadCap = ArgumentCaptor.forClass(String.class);

		then(kafkaTemplate).should(times(2)).send(
			topicCap.capture(),
			keyCap.capture(),
			payloadCap.capture()
		);

		// 두 개 payload 중 하나는 DAILY_CHECK_IN, 하나는 STREAK_3 여야 함
		assertThat(topicCap.getAllValues()).allMatch(t -> t.equals("point.grant.requested"));
		assertThat(keyCap.getAllValues()).allMatch(k -> k.equals(memberId.toString()));

		String p1 = payloadCap.getAllValues().get(0);
		String p2 = payloadCap.getAllValues().get(1);
		String all = p1 + "||" + p2;

		// 공통 필드
		assertThat(all).contains("\"memberId\":" + memberId);
		assertThat(all).contains("\"sourceType\":\"ATTENDANCE\"");
		assertThat(all).contains("\"sourceId\":\"" + today + "\"");
		assertThat(all).contains("\"occurredAt\":\"" + occurredAt);

		// 각각의 액션 타입/금액/내용/디듑키
		assertThat(all).contains("\"actionType\":\"DAILY_CHECK_IN\"")
			.contains("\"amount\":10")
			.contains("\"content\":\"출석 체크\"")
			.contains("\"dedupKey\":\"ATTEND:" + memberId + ":" + today + "\"");

		assertThat(all).contains("\"actionType\":\"STREAK_3\"")
			.contains("\"amount\":30")
			.contains("\"content\":\"출석 3일 연속 보너스\"")
			.contains("\"dedupKey\":\"ATTEND_STREAK3:" + memberId + ":" + today + "\"");

		// 반환값 정책
		assertThat(result.attended()).isTrue();
		assertThat(result.streak()).isEqualTo(3);
		assertThat(result.bestStreak()).isEqualTo(3);
		assertThat(result.grantedAmount()).isEqualTo(10);
		assertThat(result.balanceAfter()).isNull();
	}

	@Test
	@DisplayName("연속 7일 달성 시 기본 + STREAK_7 보너스가 2회 퍼블리시된다")
	void grantDailyAndStreak7() {
		// given
		stubKafkaSend();

		Long memberId = 4L;
		LocalDateTime occurredAt = LocalDateTime.of(2025, 8, 23, 6, 45);
		LocalDate today = occurredAt.atZone(KST).toLocalDate();

		Member member = mock(Member.class);
		given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
		given(member.markAttendance(today)).willReturn(true);
		given(member.getAttendanceStreak()).willReturn(7);
		given(member.getAttendanceBestStreak()).willReturn(7);

		// when
		AttendanceResult result = service.checkInAndReward(memberId, occurredAt);

		// then
		then(memberRepository).should().save(member);

		ArgumentCaptor<String> topicCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCap   = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> payloadCap = ArgumentCaptor.forClass(String.class);

		then(kafkaTemplate).should(times(2)).send(
			topicCap.capture(),
			keyCap.capture(),
			payloadCap.capture()
		);

		assertThat(topicCap.getAllValues()).allMatch(t -> t.equals("point.grant.requested"));
		assertThat(keyCap.getAllValues()).allMatch(k -> k.equals(memberId.toString()));

		String p1 = payloadCap.getAllValues().get(0);
		String p2 = payloadCap.getAllValues().get(1);
		String all = p1 + "||" + p2;

		assertThat(all).contains("\"memberId\":" + memberId);
		assertThat(all).contains("\"sourceType\":\"ATTENDANCE\"");
		assertThat(all).contains("\"sourceId\":\"" + today + "\"");
		assertThat(all).contains("\"occurredAt\":\"" + occurredAt);

		assertThat(all).contains("\"actionType\":\"DAILY_CHECK_IN\"")
			.contains("\"amount\":10")
			.contains("\"content\":\"출석 체크\"")
			.contains("\"dedupKey\":\"ATTEND:" + memberId + ":" + today + "\"");

		assertThat(all).contains("\"actionType\":\"STREAK_7\"")
			.contains("\"amount\":70")
			.contains("\"content\":\"출석 7일 연속 보너스\"")
			.contains("\"dedupKey\":\"ATTEND_STREAK7:" + memberId + ":" + today + "\"");

		assertThat(result.attended()).isTrue();
		assertThat(result.streak()).isEqualTo(7);
		assertThat(result.bestStreak()).isEqualTo(7);
		assertThat(result.grantedAmount()).isEqualTo(10);
		assertThat(result.balanceAfter()).isNull();
	}
}