package com.grow.member_service.member.application.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.grow.member_service.member.domain.model.Member;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberNotificationPublisher {

	private final ApplicationEventPublisher publisher;

	/** 온보딩 미완료 시 알림 이벤트 발행 */
	public void publishOnboardingReminders(Member m) {
		if (m.getAdditionalInfo() == null
			|| m.getAdditionalInfo().getAddress() == null
			|| m.getAdditionalInfo().getAddress().isBlank()) {
			publisher.publishEvent(new MemberNotificationEvent(
				m.getMemberId(),
				"SERVICE_NOTICE",
				"ADDR_REMINDER",
				"주소 정보 미입력",
				"더 정확한 매칭을 위해 주소 정보를 등록해 주세요.",
				LocalDateTime.now()
			));
		}

		if (!m.isPhoneVerified()) {
			publisher.publishEvent(new MemberNotificationEvent(
				m.getMemberId(),
				"SERVICE_NOTICE",
				"PHONE_REMINDER",
				"휴대폰 인증 미완료",
				"보안 강화를 위해 휴대폰 번호 인증을 완료해 주세요.",
				LocalDateTime.now()
			));
		}
	}

	/** 휴대폰 인증 성공 알림 이벤트 발행 */
	public void publishPhoneVerifiedSuccess(Long memberId) {
		publisher.publishEvent(new MemberNotificationEvent(
			memberId,
			"SERVICE_NOTICE",
			"PHONE_VERIFIED",
			"인증 완료",
			"휴대폰 번호 인증이 완료되었습니다.",
			LocalDateTime.now()
		));
	}
}