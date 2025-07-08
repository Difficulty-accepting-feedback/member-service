package com.grow.member_service.auth.infra.oauth2;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grow.member_service.auth.infra.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtTokenProvider jwtProvider;
	private final String frontUrl = "http://localhost:3000/oauth/redirect";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest req,
		HttpServletResponse res,
		Authentication auth) throws IOException {
		DefaultOAuth2User oauthUser = (DefaultOAuth2User) auth.getPrincipal();
		Long memberId = Long.valueOf(oauthUser.getAttributes().get("memberId").toString());

		// JWT 생성
		String accessToken  = jwtProvider.createAccessToken(memberId);
		String refreshToken = jwtProvider.createRefreshToken(memberId);

		// HttpOnly 쿠키에 담기 (필요하면 리프레시 쿠키도 추가)
		ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
			.httpOnly(true).secure(true).path("/").maxAge(Duration.ofMinutes(15)).sameSite("Strict").build();
		ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
			.httpOnly(true).secure(true).path("/").maxAge(Duration.ofDays(7)).sameSite("Strict").build();

		res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

		// 최종 리디렉트
		getRedirectStrategy().sendRedirect(req, res, frontUrl);
	}
}