package com.grow.member_service.auth.infra.security.jwt;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

	private JwtTokenProvider provider;

	@BeforeEach
	void setUp() {
		JwtProperties props = new JwtProperties();
		props.setSecret("test-secret-key-test-secret-key-1234567890");
		props.setAccessTokenExpiration(1);  // 1초 유효 기간
		props.setRefreshTokenExpiration(2); // 2초 유효 기간

		provider = new JwtTokenProvider(props);
		provider.init(); // @PostConstruct 대체
	}

	@Test
	@DisplayName("발급된 액세스 토큰은 검증 통과한다")
	void accessTokenValidate() {
		String token = provider.createAccessToken(42L);
		assertThat(provider.validateToken(token)).isTrue();
	}

	@Test
	@DisplayName("발급된 리프레시 토큰은 검증 통과한다")
	void refreshTokenValidate() {
		String token = provider.createRefreshToken(99L);
		assertThat(provider.validateToken(token)).isTrue();
	}

	@Test
	@DisplayName("만료된 토큰은 검증에 실패한다")
	void tokenExpired() throws InterruptedException {
		// Access 토큰 유효기간 1초
		String token = provider.createAccessToken(1L);
		Thread.sleep(Duration.ofSeconds(2).toMillis());
		assertThat(provider.validateToken(token)).isFalse();
	}

	@Test
	@DisplayName("토큰에서 MemberId를 정확히 추출한다")
	void extractMemberId() {
		String token = provider.createAccessToken(123L);
		Long memberId = provider.getMemberId(token);
		assertThat(memberId).isEqualTo(123L);
	}

	@Test
	@DisplayName("잘못된 토큰은 검증에 실패하고, getMemberId는 예외를 던진다")
	void invalidToken() {
		assertThat(provider.validateToken("invalid")).isFalse();
		assertThatThrownBy(() -> provider.getMemberId("invalid"))
			.isInstanceOf(RuntimeException.class);
	}
}