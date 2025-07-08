package com.grow.member_service.auth.application.dto;

public class TokenResponse {
	private final String accessToken;
	private final String refreshToken;

	// public 생성자
	public TokenResponse(String accessToken, String refreshToken) {
		this.accessToken  = accessToken;
		this.refreshToken = refreshToken;
	}

	// Getter
	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
}