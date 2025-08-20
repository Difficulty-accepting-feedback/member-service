package com.grow.member_service.member.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NotificationWebClientConfig {

	/**
	 * Notification 서비스와 통신하기 위한 WebClient를 설정합니다.
	 * @param builder
	 * @param baseUrl
	 * @return
	 */
	@Bean
	public WebClient notificationWebClient(
		WebClient.Builder builder,
		@Value("${service.notification.base-url}") String baseUrl
	) {
		return builder.baseUrl(baseUrl).build();
	}
}