package com.grow.member_service.member.infra.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

	/**
	 * 시스템 기본 시간대의 Clock 빈 등록
	 */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}