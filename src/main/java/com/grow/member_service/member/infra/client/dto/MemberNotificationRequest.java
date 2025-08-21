package com.grow.member_service.member.infra.client.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberNotificationRequest {
	private String notificationType;
	private Long memberId;
	private String title;
	private String content;
	private String code;
	private LocalDateTime occurredAt;
}