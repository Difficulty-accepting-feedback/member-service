package com.grow.member_service.member.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NotificationWebClientConfig {

	@Bean
	public WebClient notificationWebClient(
		WebClient.Builder builder,
		@Value("${service.notification.base-url}") String baseUrl
	) {
		return builder.baseUrl(baseUrl).build();
	}
}