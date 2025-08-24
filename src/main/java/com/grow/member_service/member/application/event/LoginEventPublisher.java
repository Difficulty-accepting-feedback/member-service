package com.grow.member_service.member.application.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginEventPublisher {
	private final ApplicationEventPublisher publisher;

	/**
	 * 로그인 성공 이벤트를 발행합니다.
	 * @param memberId
	 */
	public void publishLoginSucceeded(Long memberId) {
		publisher.publishEvent(new LoginSucceededEvent(memberId, LocalDateTime.now()));
	}
}