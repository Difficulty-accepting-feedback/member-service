package com.grow.member_service.auth.infra.security.oauth2.handler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtTokenProvider;

class OAuth2AuthenticationSuccessHandlerTest {

	private OAuth2AuthenticationSuccessHandler handler;

	@BeforeEach
	void setUp() {
		JwtProperties props = new JwtProperties();
		props.setSecret("a-very-secret-key-that-is-at-least-32-bytes!");
		props.setAccessTokenExpiration(3600);
		props.setRefreshTokenExpiration(7200);

		JwtTokenProvider jwtProvider = new JwtTokenProvider(props);
		jwtProvider.init();

		handler = new OAuth2AuthenticationSuccessHandler(jwtProvider, props);
	}

	@Test
	@DisplayName("onAuthenticationSuccess: 토큰 쿠키 설정 후 frontUrl로 리다이렉트")
	void onAuthenticationSuccess_setsCookiesAndRedirects() throws Exception {
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

		handler.onAuthenticationSuccess(req, res, auth);

		// HttpOnly 쿠키가 설정되었는지
		assertThat(res.getCookie("access_token")).isNotNull();
		assertThat(res.getCookie("refresh_token")).isNotNull();

		// 올바른 URL로 리다이렉트되었는지
		assertThat(res.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect");
	}
}