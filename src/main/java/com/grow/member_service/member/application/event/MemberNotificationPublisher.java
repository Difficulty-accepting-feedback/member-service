package com.grow.member_service.member.application.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberNotificationPublisher {

	private final ApplicationEventPublisher publisher;

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