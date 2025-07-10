package com.grow.member_service.auth.infra.security.oauth2.handler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.grow.member_service.auth.infra.config.OAuthProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtTokenProvider;
import com.grow.member_service.member.application.service.PhoneVerificationService;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

	private OAuth2AuthenticationSuccessHandler handler;

	@Mock
	private PhoneVerificationService phoneVerificationService;

	@Mock
	private OAuthProperties oauthProperties;

	@BeforeEach
	void setUp() {
		// JWT 프로퍼티 & 프로바이더 초기화
		JwtProperties props = new JwtProperties();
		props.setSecret("a-very-secret-key-that-is-at-least-32-bytes!");
		props.setAccessTokenExpiration(3600);
		props.setRefreshTokenExpiration(7200);
		JwtTokenProvider jwtProvider = new JwtTokenProvider(props);
		jwtProvider.init();

		// OAuth 리다이렉트 URI, 검증 상태 설정
		given(oauthProperties.getRedirectUri())
			.willReturn("http://localhost:3000/oauth/redirect");
		// 회원 42는 아직 폰 인증 안 함
		given(phoneVerificationService.isPhoneVerified(42L))
			.willReturn(false);

		handler = new OAuth2AuthenticationSuccessHandler(
			jwtProvider,
			props,
			phoneVerificationService,
			oauthProperties
		);
	}

	@Test
	@DisplayName("onAuthenticationSuccess: 쿠키 세팅 후 enter-phone 스텝으로 리다이렉트")
	void onAuthenticationSuccess_setsCookiesAndRedirectsWithStep() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();

		// DefaultOAuth2User 생성 (principal)
		Map<String, Object> attrs = Map.of("memberId", "42");
		DefaultOAuth2User principal = new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			attrs,
			"memberId"
		);
		Authentication auth = mock(Authentication.class);
		when(auth.getPrincipal()).thenReturn(principal);

		// 실행
		handler.onAuthenticationSuccess(req, res, auth);

		// HttpOnly 쿠키 확인
		assertThat(res.getCookie("access_token")).isNotNull();
		assertThat(res.getCookie("refresh_token")).isNotNull();

		// enter-phone 파라미터 포함된 리다이렉트 URL 확인
		assertThat(res.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect?step=enter-phone");
	}

	@Test
	@DisplayName("onAuthenticationSuccess: 이미 인증된 회원은 complete 스텝으로 리다이렉트")
	void onAuthenticationSuccess_redirectsToCompleteWhenVerified() throws Exception {
		// 이미 인증된 시나리오
		given(phoneVerificationService.isPhoneVerified(42L)).willReturn(true);

		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();

		Map<String, Object> attrs = Map.of("memberId", "42");
		DefaultOAuth2User principal = new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			attrs,
			"memberId"
		);
		Authentication auth = mock(Authentication.class);
		when(auth.getPrincipal()).thenReturn(principal);

		handler.onAuthenticationSuccess(req, res, auth);

		assertThat(res.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect?step=complete");
	}
}