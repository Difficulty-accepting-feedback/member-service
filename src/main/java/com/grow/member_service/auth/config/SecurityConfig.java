package com.grow.member_service.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.grow.member_service.auth.infra.security.jwt.JwtAuthenticationFilter;
import com.grow.member_service.auth.infra.security.oauth2.adapter.CustomOAuth2Service;
import com.grow.member_service.auth.infra.security.oauth2.handler.OAuth2AuthenticationFailureHandler;
import com.grow.member_service.auth.infra.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomOAuth2Service oauth2Service;
	private final OAuth2AuthenticationSuccessHandler successHandler;
	private final OAuth2AuthenticationFailureHandler failureHandler;
	private final JwtAuthenticationFilter jwtFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/members/oauth/**").permitAll()
				.anyRequest().authenticated()
			)
			// OAuth2 로그인 설정
			.oauth2Login(o -> o
				.userInfoEndpoint(u -> u.userService(oauth2Service))
				.successHandler(successHandler)
				.failureHandler(failureHandler)
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}