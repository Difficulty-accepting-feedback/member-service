package com.grow.member_service.member.infra.client.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationRequest {
	private final Long memberId;
	private final String content;
	private final String notificationType; // 알림서비스 enum 이름 그대로 사용
	private final LocalDateTime timestamp;
}