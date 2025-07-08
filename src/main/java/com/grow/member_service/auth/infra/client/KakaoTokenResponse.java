package com.grow.member_service.auth.infra.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 OAuth 토큰 엔드포인트 응답 매핑용 DTO
 */
@Getter
@NoArgsConstructor
public class KakaoTokenResponse {

	// 액세스 토큰
	@JsonProperty("access_token")
	private String accessToken;

	// 리프레시 토큰
	@JsonProperty("refresh_token")
	private String refreshToken;

	// 토큰 타입 (bearer)
	@JsonProperty("token_type")
	private String tokenType;

	// 액세스 토큰 만료 시간(초)
	@JsonProperty("expires_in")
	private Long expiresIn;

	// 리프레시 토큰 만료 시간(초)
	@JsonProperty("refresh_token_expires_in")
	private Long refreshTokenExpiresIn;
}