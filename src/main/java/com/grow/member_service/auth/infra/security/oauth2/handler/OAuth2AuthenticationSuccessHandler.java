package com.grow.member_service.auth.infra.security.oauth2.handler;

import static com.grow.member_service.global.exception.ErrorCode.*;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtTokenProvider;
import com.grow.member_service.common.OAuthException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2 인증 성공 시 JWT 토큰을 생성하고
 * HttpOnly 쿠키에 담아 리다이렉트하는 핸들러
 * 지정된 URL로 리다이렉트 (변경 필요)
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtTokenProvider jwtProvider;
	private final JwtProperties jwtProperties;

	private final String frontUrl = "http://localhost:3000/oauth/redirect";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest req,
		HttpServletResponse res,
		Authentication auth) throws IOException {
		DefaultOAuth2User oauthUser = (DefaultOAuth2User) auth.getPrincipal();
		Long memberId;
		try {
			memberId = Long.valueOf(oauthUser.getAttributes().get("memberId").toString());
		} catch (Exception e) {
			throw new OAuthException(OAUTH_MEMBER_ID_PARSE_ERROR, e);
		}
		// JWT 생성
		String accessToken = jwtProvider.createAccessToken(memberId);
		String refreshToken = jwtProvider.createRefreshToken(memberId);

		// 만료 시간 가져오기
		Duration accessDuration = jwtProperties.getAccessTokenExpiryDuration();
		Duration refreshDuration = jwtProperties.getRefreshTokenExpiryDuration();

		// HttpOnly 쿠키에 담기
		ResponseCookie accessCookie = ResponseCookie
			.from("access_token", accessToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(accessDuration)
			.sameSite("Strict")
			.build();

		ResponseCookie refreshCookie = ResponseCookie
			.from("refresh_token", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(refreshDuration)
			.sameSite("Strict")
			.build();

		res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

		getRedirectStrategy().sendRedirect(req, res, frontUrl);
	}
}