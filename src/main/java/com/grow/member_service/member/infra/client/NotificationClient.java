package com.grow.member_service.member.infra.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.grow.member_service.member.infra.client.dto.NotificationRequest;

// TODO(GATEWAY): 게이트웨이 도입 시 base-url 교체 또는 제거.
@FeignClient(
	name = "notification-service",
	url  = "${clients.notification.base-url}",
	path = "/notifications"
)
public interface NotificationClient {

	@PostMapping
	void sendServiceNotice(@RequestBody NotificationRequest request);
}