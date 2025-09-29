package com.grow.member_service.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class MicrometerAopConfig {
	@Bean
	TimedAspect timedAspect(MeterRegistry registry) { return new TimedAspect(registry); }
	@Bean
	CountedAspect countedAspect(MeterRegistry registry) { return new CountedAspect(registry); }
}