// src/main/java/com/grow/member_service/member/infra/client/NotificationFeignAdapter.java
package com.grow.member_service.member.infra.adapter;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.grow.member_service.member.application.port.NotificationPort;
import com.grow.member_service.member.infra.client.NotificationClient;
import com.grow.member_service.member.infra.client.dto.NotificationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAdapter implements NotificationPort {

	private final NotificationClient client;

	/**
	 * 서비스 공지 알림을 전송한다.
	 * 바디에 memberId, content, notificationType, timestamp를 담아 전송한다.
	 */
	@Override
	public void sendServiceNotice(Long memberId, String content) {
		NotificationRequest body = NotificationRequest.builder()
			.memberId(memberId)
			.content(content)
			.notificationType("SERVICE_NOTICE")
			.timestamp(LocalDateTime.now())
			.build();

		try {
			client.sendServiceNotice(body); // 동기 호출
			log.info("[알림 전송 요청] SERVICE_NOTICE memberId={}, contentLen={}", memberId, content.length());
		} catch (Exception e) {
			log.warn("[알림 전송 실패] SERVICE_NOTICE memberId={}, ex={}", memberId, e.toString());
		}
	}
}