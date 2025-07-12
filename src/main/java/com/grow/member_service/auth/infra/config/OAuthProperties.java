package com.grow.member_service.auth.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class OAuthProperties {

	@NotBlank
	private String redirectUri;
}