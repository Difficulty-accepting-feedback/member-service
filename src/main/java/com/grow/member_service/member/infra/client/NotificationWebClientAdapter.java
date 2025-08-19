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