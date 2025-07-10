package com.grow.member_service.auth.infra.security.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

class JwtAuthenticationFilterTest {

	private JwtAuthenticationFilter filter;
	private JwtTokenProvider tokenProvider;
	private JwtProperties props;

	@BeforeEach
	void setUp() {
		tokenProvider = mock(JwtTokenProvider.class);
		props = mock(JwtProperties.class);
		filter = new JwtAuthenticationFilter(tokenProvider, props);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("유효한 access_token 쿠키가 있으면 인증 정보가 설정된다")
	void validAccessToken_setsAuthentication() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();
		req.setCookies(new Cookie("access_token", "validToken"));

		FilterChain chain = mock(FilterChain.class);
		when(tokenProvider.validateToken("validToken")).thenReturn(true);
		when(tokenProvider.getMemberId("validToken")).thenReturn(100L);

		filter.doFilterInternal(req, res, chain);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getPrincipal()).isEqualTo(100L);
		verify(chain).doFilter(req, res);
	}

	@Test
	@DisplayName("만료된 access_token + 유효한 refresh_token이 있으면 토큰 재발급 및 인증")
	void expiredAccess_validRefreshToken_rotatesAndAuthenticates() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();
		req.setCookies(
			new Cookie("access_token", "expiredToken"),
			new Cookie("refresh_token", "refreshToken")
		);

		FilterChain chain = mock(FilterChain.class);

		// access 만료 시나리오
		when(tokenProvider.validateToken("expiredToken"))
			.thenThrow(new ExpiredJwtException(null, null, null));

		// refresh 유효 시나리오
		when(tokenProvider.validateToken("refreshToken")).thenReturn(true);
		when(tokenProvider.getMemberId("refreshToken")).thenReturn(200L);

		when(props.getAccessTokenExpiryDuration())
			.thenReturn(Duration.ofMinutes(15));
		when(props.getRefreshTokenExpiryDuration())
			.thenReturn(Duration.ofDays(7));

		when(tokenProvider.createAccessToken(200L)).thenReturn("newAccess");
		when(tokenProvider.createRefreshToken(200L)).thenReturn("newRefresh");

		filter.doFilterInternal(req, res, chain);

		Cookie newAccess = res.getCookie("access_token");
		Cookie newRefresh = res.getCookie("refresh_token");
		assertThat(newAccess.getValue()).isEqualTo("newAccess");
		assertThat(newRefresh.getValue()).isEqualTo("newRefresh");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getPrincipal()).isEqualTo(200L);
		verify(chain).doFilter(req, res);
	}

	@Test
	@DisplayName("토큰이 없으면 다음 필터로 넘어간다")
	void noToken_chainContinues() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();

		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(req, res, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(chain).doFilter(req, res);
	}
}