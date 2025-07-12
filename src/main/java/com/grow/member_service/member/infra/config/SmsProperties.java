package com.grow.member_service.member.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "sms")
@Getter
@Setter
public class SmsProperties {

	@NotBlank
	private String apiKey;

	@NotBlank
	private String apiSecret;

	@NotBlank
	private String from;
}