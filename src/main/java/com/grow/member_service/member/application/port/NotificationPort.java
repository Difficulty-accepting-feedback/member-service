package com.grow.member_service.member.application.port;

public interface NotificationPort {
	void sendServiceNotice(Long memberId, String content);
}