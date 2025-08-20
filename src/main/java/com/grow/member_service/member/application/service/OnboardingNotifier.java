package com.grow.member_service.member.application.service;

import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.grow.member_service.member.application.port.NotificationPort;
import com.grow.member_service.member.domain.model.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnboardingNotifier {

	private final NotificationPort notificationPort;
	private final ObjectProvider<StringRedisTemplate> redisProvider;

	private static final Duration DEDUPE_TTL = Duration.ofHours(12);

	/** 주소 미등록, 휴대폰 미인증이면 서비스 공지 알림을 보낸다(12시간 중복 방지). */
	public void pushRemindersIfNeeded(Member m) {
		// 1) 주소 미등록
		if (m.getAdditionalInfo() == null
			|| m.getAdditionalInfo().getAddress() == null
			|| m.getAdditionalInfo().getAddress().isBlank()) {
			if (acquireOnce("notify:onboard:addr:" + m.getMemberId(), DEDUPE_TTL)) {
				notificationPort.sendServiceNotice(
					m.getMemberId(),
					"더 정확한 매칭을 위해 주소 정보를 등록해 주세요."
				);
			}
		}
		// 2) 휴대폰 미인증
		if (!m.isPhoneVerified()) {
			if (acquireOnce("notify:onboard:phone:" + m.getMemberId(), DEDUPE_TTL)) {
				notificationPort.sendServiceNotice(
					m.getMemberId(),
					"보안 강화를 위해 휴대폰 번호 인증을 완료해 주세요."
				);
			}
		}
	}

	/** 휴대폰 인증 완료 알림 */
	public void sendPhoneVerifiedSuccess(Long memberId) {
		notificationPort.sendServiceNotice(memberId, "휴대폰 번호 인증이 완료되었습니다.");
	}

	/**
	 * 중복 방지용 Redis 키를 획득하여
	 * 12시간 동안 중복 알림을 방지한다.
	 * @param key
	 * @param ttl
	 * @return
	 */
	private boolean acquireOnce(String key, Duration ttl) {
		StringRedisTemplate r = redisProvider.getIfAvailable();
		if (r == null) return true;                // 로컬/테스트 환경: 그냥 발송
		Boolean ok = r.opsForValue().setIfAbsent(key, "1", ttl);
		return Boolean.TRUE.equals(ok);
	}
}