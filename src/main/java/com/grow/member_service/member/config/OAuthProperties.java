package com.grow.member_service.member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.kakao")
public class OAuthProperties {
	private String clientId;
	private String clientSecret;
	private String redirectUri;

}