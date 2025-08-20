package com.grow.member_service.member.application.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.grow.member_service.member.application.port.NotificationPort;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;

@ExtendWith(MockitoExtension.class)
class OnboardingNotifierTest {

	@Mock
	NotificationPort notificationPort;

	@Mock
	ObjectProvider<StringRedisTemplate> redisProvider;

	@Mock
	StringRedisTemplate redisTemplate;

	@Mock
	ValueOperations<String, String> valueOps;

	@InjectMocks
	OnboardingNotifier notifier;

	private static final String MSG_ADDR  = "더 정확한 매칭을 위해 주소 정보를 등록해 주세요.";
	private static final String MSG_PHONE = "보안 강화를 위해 휴대폰 번호 인증을 완료해 주세요.";

	/** 멤버 생성 유틸: address가 null이면 AdditionalInfo 미존재로 간주 */
	private Member member(Long id, String address, boolean phoneVerified) {
		Member m = mock(Member.class);
		when(m.getMemberId()).thenReturn(id);
		when(m.isPhoneVerified()).thenReturn(phoneVerified);

		if (address != null) {
			MemberAdditionalInfo info = mock(MemberAdditionalInfo.class);
			when(info.getAddress()).thenReturn(address);
			when(m.getAdditionalInfo()).thenReturn(info);
		} else {
			when(m.getAdditionalInfo()).thenReturn(null);
		}
		return m;
	}

	private void enableRedis() {
		when(redisProvider.getIfAvailable()).thenReturn(redisTemplate);
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
	}

	@Nested
	@DisplayName("pushRemindersIfNeeded")
	class PushRemindersIfNeeded {

		@Test
		@DisplayName("주소 미등록 & 휴대폰 미인증 → 두 건 모두 발송 (각각 첫 setIfAbsent 성공)")
		void sendBothWhenFirstAcquire() {
			// given
			enableRedis();
			when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
				.thenReturn(true, true);

			Member m = member(12L, null, false);

			// when
			notifier.pushRemindersIfNeeded(m);

			// then
			verify(notificationPort).sendServiceNotice(12L, MSG_ADDR);
			verify(notificationPort).sendServiceNotice(12L, MSG_PHONE);
			verifyNoMoreInteractions(notificationPort);
		}

		@Test
		@DisplayName("주소 미등록이나 dedupe 키 존재 → 주소 알림 미발송")
		void skipAddressWhenDedupeExists() {
			// given
			enableRedis();
			when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
				.thenReturn(false); // 주소 키 이미 존재

			Member m = member(34L, null, true); // 휴대폰은 이미 인증됨

			// when
			notifier.pushRemindersIfNeeded(m);

			// then
			verifyNoInteractions(notificationPort);
		}

		@Test
		@DisplayName("휴대폰 미인증이나 dedupe 키 존재 → 휴대폰 알림 미발송")
		void skipPhoneWhenDedupeExists() {
			// given
			enableRedis();
			when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
				.thenReturn(false); // 휴대폰 키 이미 존재

			Member m = member(56L, "경기도 남양주시 별내로", false);

			// when
			notifier.pushRemindersIfNeeded(m);

			// then
			verifyNoInteractions(notificationPort);
		}

		@MockitoSettings(strictness = Strictness.LENIENT)
		@Test
		@DisplayName("주소 있음 & 휴대폰 인증 완료 → 어떠한 알림도 발송하지 않음 (Redis 스터빙 불필요)")
		void noSendWhenHealthy() {
			// given
			Member m = member(78L, "경기도 남양주시 별내로", true);

			// when
			notifier.pushRemindersIfNeeded(m);

			// then
			verifyNoInteractions(notificationPort);
			// Redis 호출 자체가 없어야 함
			verifyNoInteractions(redisProvider, redisTemplate, valueOps);
		}

		@Test
		@DisplayName("Redis 미사용(null) 환경 → acquireOnce가 true로 간주되어 발송")
		void sendWhenRedisUnavailable() {
			// given
			when(redisProvider.getIfAvailable()).thenReturn(null);
			Member m = member(90L, null, false);

			// when
			notifier.pushRemindersIfNeeded(m);

			// then
			verify(notificationPort).sendServiceNotice(90L, MSG_ADDR);
			verify(notificationPort).sendServiceNotice(90L, MSG_PHONE);
			verifyNoMoreInteractions(notificationPort);
		}

		@Test
		@DisplayName("주소는 빈 문자열로 등록된 경우도 미등록으로 간주 → 주소 알림 발송")
		void blankAddressTreatedAsMissing() {
			// given
			enableRedis();
			// 주소 키만 성공(true), 휴대폰은 이미 인증되어 호출 안 됨
			when(valueOps.setIfAbsent(contains("notify:onboard:addr:"), anyString(), any(Duration.class)))
				.thenReturn(true);

			// MemberAdditionalInfo.getAddress()가 " "인 케이스를 만들기 위해 별도 mock
			Member m = mock(Member.class);
			when(m.getMemberId()).thenReturn(101L);
			when(m.isPhoneVerified()).thenReturn(true);
			MemberAdditionalInfo info = mock(MemberAdditionalInfo.class);
			when(info.getAddress()).thenReturn("   "); // blank
			when(m.getAdditionalInfo()).thenReturn(info);

			// when
			notifier.pushRemindersIfNeeded(m);

			// then
			verify(notificationPort).sendServiceNotice(101L, MSG_ADDR);
			verifyNoMoreInteractions(notificationPort);
		}
	}

	@Test
	@DisplayName("sendPhoneVerifiedSuccess: 휴대폰 인증 완료 알림 단건 발송")
	void sendPhoneVerifiedSuccess() {
		// when
		notifier.sendPhoneVerifiedSuccess(201L);

		// then
		verify(notificationPort).sendServiceNotice(201L, "휴대폰 번호 인증이 완료되었습니다.");
		verifyNoMoreInteractions(notificationPort);
	}
}