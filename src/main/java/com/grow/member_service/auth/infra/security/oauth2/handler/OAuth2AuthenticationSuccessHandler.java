package com.grow.member_service.auth.infra.security.oauth2.handler;

import static com.grow.member_service.global.exception.ErrorCode.*;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.grow.member_service.auth.infra.config.OAuthProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtTokenProvider;
import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.member.application.service.impl.PhoneVerificationServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2 인증 성공 시 JWT 토큰을 생성하고
 * HttpOnly 쿠키에 담아 리다이렉트하는 핸들러
 * 지정된 URL로 리다이렉트 (변경 필요-> 핸드폰 인증 화면으로 바로 리다이렉트)
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtTokenProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final PhoneVerificationServiceImpl phoneVerificationServiceImpl;
	private final OAuthProperties oauthProperties;

	/**
	 * 인증 성공 후 호출되는 메서드
	 * @param req HTTP 요청 객체
	 * @param res HTTP 응답 객체
	 * @param auth 인증 정보 객체
	 * @throws IOException 입출력 예외
	 */
	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest req,
		HttpServletResponse res,
		Authentication auth
	) throws IOException {
		// 멤버 ID 추출
		Long memberId = extractMemberId((DefaultOAuth2User) auth.getPrincipal());

		// JWT 생성
		String accessToken = jwtProvider.createAccessToken(memberId);
		String refreshToken = jwtProvider.createRefreshToken(memberId);
		Duration accessDuration = jwtProperties.getAccessTokenExpiryDuration();
		Duration refreshDuration = jwtProperties.getRefreshTokenExpiryDuration();

		// HttpOnly 쿠키에 담기
		addAuthCookie(res, "access_token", accessToken, accessDuration);
		addAuthCookie(res, "refresh_token", refreshToken, refreshDuration);

		// 핸드폰 인증 여부 확인
		boolean verified = phoneVerificationServiceImpl.isPhoneVerified(memberId);
		String step = verified ? "complete" : "enter-phone"; // 추후에 주소 변경

		// provider 정보 추출
		String provider = (auth instanceof OAuth2AuthenticationToken oat)
			? oat.getAuthorizedClientRegistrationId()
			: "unknown";


		// frontUrl 로 분기 리다이렉트
		String target = UriComponentsBuilder
			.fromUriString(oauthProperties.getRedirectUri())
			.queryParam("step", step)
			.queryParam("provider", provider)
			.build()
			.toUriString();

		getRedirectStrategy().sendRedirect(req, res, target);
	}

	/**
	 * OAuth2User에서 멤버 ID를 추출합니다.
	 * @param oauthUser 인증된 OAuth2 사용자 정보
	 * @return 멤버 ID
	 */
	private Long extractMemberId(DefaultOAuth2User oauthUser) {
		try {
			return Long.valueOf(oauthUser.getAttributes().get("memberId").toString());
		} catch (Exception e) {
			throw new OAuthException(OAUTH_MEMBER_ID_PARSE_ERROR, e);
		}
	}

	/**
	 * HttpOnly 쿠키에 인증 토큰을 추가합니다.
	 * @param res HTTP 응답 객체
	 * @param name 쿠키 이름
	 * @param value 쿠키 값
	 * @param maxAge 쿠키 최대 유효 기간
	 */
	private void addAuthCookie(HttpServletResponse res, String name, String value, Duration maxAge) {
		ResponseCookie cookie = ResponseCookie
			.from(name, value)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(maxAge)
			.sameSite("None")
			.build();
		res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}