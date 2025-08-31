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
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.grow.member_service.auth.infra.config.OAuthProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.auth.infra.security.jwt.JwtTokenProvider;
import com.grow.member_service.member.application.event.LoginEventPublisher;
import com.grow.member_service.member.application.service.PhoneVerificationService;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

	private OAuth2AuthenticationSuccessHandler handler;

	@Mock
	private PhoneVerificationService phoneVerificationServiceImpl;

	@Mock
	private OAuthProperties oauthProperties;

	@Mock
	private LoginEventPublisher loginEventPublisher;

	private JwtProperties jwtProps;

	@BeforeEach
	void setUp() {
		// JWT 설정
		jwtProps = new JwtProperties();
		jwtProps.setSecret("a-very-secret-key-that-is-at-least-32-bytes!");
		jwtProps.setAccessTokenExpiration(3600);
		jwtProps.setRefreshTokenExpiration(7200);

		JwtTokenProvider jwtProvider = new JwtTokenProvider(jwtProps);
		jwtProvider.init();

		// 리다이렉트 URI
		given(oauthProperties.getRedirectUri())
			.willReturn("http://localhost:3000/oauth/redirect");

		// 기본: 폰 미인증
		given(phoneVerificationServiceImpl.isPhoneVerified(42L))
			.willReturn(false);

		handler = new OAuth2AuthenticationSuccessHandler(
			jwtProvider,
			jwtProps,
			phoneVerificationServiceImpl,
			oauthProperties,
			loginEventPublisher
		);
	}

	private OAuth2AuthenticationToken oauthToken(String provider) {
		Map<String, Object> attrs = Map.of("memberId", "42");
		DefaultOAuth2User principal = new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			attrs,
			"memberId"
		);
		return new OAuth2AuthenticationToken(
			principal,
			principal.getAuthorities(),
			provider
		);
	}

	@Test
	@DisplayName("성공시 HttpOnly 쿠키 설정 후 폰 미인증은 enter-phone + provider 포함 리다이렉트")
	void onAuthenticationSuccess_setsCookiesAndRedirects_withProvider_whenNotVerified() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();

		// provider=google
		OAuth2AuthenticationToken auth = oauthToken("google");

		handler.onAuthenticationSuccess(req, res, auth);

		// Set-Cookie 헤더 검증
		List<String> cookies = res.getHeaders(HttpHeaders.SET_COOKIE);
		assertThat(cookies).isNotEmpty();
		assertThat(cookies).anySatisfy(c -> {
			assertThat(c).contains("access_token=");
			assertThat(c).contains("HttpOnly");
			assertThat(c).contains("SameSite=None");
			assertThat(c).contains("Secure");
			assertThat(c).contains("Path=/");
		});
		assertThat(cookies).anySatisfy(c -> assertThat(c).contains("refresh_token="));

		// 리다이렉트 URL (step + provider)
		assertThat(res.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect?step=enter-phone&provider=google");
	}

	@Test
	@DisplayName("성공시 이미 인증된 회원은 complete + provider 포함 리다이렉트")
	void onAuthenticationSuccess_redirectsToComplete_whenVerified() throws Exception {
		given(phoneVerificationServiceImpl.isPhoneVerified(42L)).willReturn(true);

		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();

		OAuth2AuthenticationToken auth = oauthToken("google");

		handler.onAuthenticationSuccess(req, res, auth);

		assertThat(res.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect?step=complete&provider=google");
	}

	@Test
	@DisplayName("OAuth2AuthenticationToken이 아니면 provider=unknown으로 리다이렉트")
	void onAuthenticationSuccess_providerUnknown_whenNotOAuthToken() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();

		// OAuth2AuthenticationToken이 아닌 경우를 시뮬레이션 하기 위해
		// principal만 채운 커스텀 Authentication을 사용
		var principal = new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			Map.of("memberId", "42"),
			"memberId"
		);
		var auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
		given(auth.getPrincipal()).willReturn(principal);

		handler.onAuthenticationSuccess(req, res, auth);

		assertThat(res.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect?step=enter-phone&provider=unknown");
	}
}