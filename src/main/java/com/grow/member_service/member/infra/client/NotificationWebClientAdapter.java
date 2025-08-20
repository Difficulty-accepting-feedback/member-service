package com.grow.member_service.member.infra.client;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.grow.member_service.member.application.port.NotificationPort;
import com.grow.member_service.member.infra.client.dto.NotificationRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationWebClientAdapter implements NotificationPort {

	private final WebClient notificationWebClient;

	/**
	 * 서비스 공지 알림을 전송한다.
	 * 이 메서드는 멤버에게 서비스 관련 공지사항을 전달하는 데 사용됩니다.
	 * * @param memberId 멤버의 고유 ID
	 * * @param content 공지 내용
	 * * <p>이 메서드는 WebClient를 사용하여 비동기적으로 알림을 전송합니다.
	 * * <p>알림 유형은 "SERVICE_NOTICE"로 설정되며, 현재 시간을 타임스탬프로 포함합니다.
	 * * <p>알림 전송은 2초의 타임아웃을 설정하여 처리됩니다.
	 * * <p>이 메서드는 알림 전송 결과를 반환하지 않으며, 단순히 요청을 보냅니다.
	 * * <p>예외가 발생할 경우, WebClient의 기본 예외 처리 로직에 따라 처리됩니다.
	 */
	@Override
	public void sendServiceNotice(Long memberId, String content) {
		NotificationRequest body = NotificationRequest.builder()
			.memberId(memberId)
			.content(content)
			.notificationType("SERVICE_NOTICE")
			.timestamp(LocalDateTime.now())
			.build();

		notificationWebClient.post()
			.uri("/notifications")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.retrieve()
			.toBodilessEntity()
			.block(Duration.ofSeconds(2));
	}
}