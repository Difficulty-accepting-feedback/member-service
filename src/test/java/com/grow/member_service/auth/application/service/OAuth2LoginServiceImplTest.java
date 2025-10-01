package com.grow.member_service.auth.application.service;

import static com.grow.member_service.auth.infra.security.oauth2.OAuth2AttributeKey.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.grow.member_service.auth.application.service.impl.OAuth2LoginServiceImpl;
import com.grow.member_service.auth.infra.security.oauth2.processor.OAuth2UserProcessor;
import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.port.NicknameGeneratorPort;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OAuth2LoginServiceImplTest {

	private OAuth2LoginService oAuth2LoginService;

	@Mock private MemberRepository memberRepository;
	@Mock private OAuth2UserProcessor kakaoProcessor;
	@Mock private NicknameGeneratorPort nicknameGenerator;

	private SimpleMeterRegistry meterRegistry;
	private Clock fixedClock;

	@BeforeEach
	void init() {
		fixedClock = Clock.fixed(Instant.parse("2025-07-10T00:00:00Z"), ZoneId.of("UTC"));
		meterRegistry = new SimpleMeterRegistry();

		// Kakao 지원 및 닉네임 생성 스텁
		given(kakaoProcessor.supports(Platform.KAKAO)).willReturn(true);
		given(nicknameGenerator.generate(anyString()))
			.willAnswer(invocation -> invocation.getArgument(0));

		// 생성자 인자 순서: processors, memberRepository, nicknameGenerator, clock, meterRegistry
		oAuth2LoginService = new OAuth2LoginServiceImpl(
			List.of(kakaoProcessor),
			memberRepository,
			nicknameGenerator,
			fixedClock,
			meterRegistry
		);
	}

	@AfterEach
	void tearDown() {
		meterRegistry.close();
	}

	@Test
	@DisplayName("기존 사용자 존재 → 그대로 반환 + 성공 카운터 1 증가")
	void findExistingMember() {
		// given
		Map<String, Object> rawAttrs = Map.of();
		Map<String, Object> parsed = Map.of(
			PLATFORM_ID,   "12345",
			EMAIL,         "test@kakao.com",
			NICKNAME,      "장무영",
			PROFILE_IMAGE, "http://img.com"
		);
		Member existing = new Member(
			new MemberProfile("test@kakao.com", "장무영", "http://img.com", Platform.KAKAO, "12345"),
			new MemberAdditionalInfo(null, null),
			fixedClock
		);

		given(kakaoProcessor.parseAttributes(rawAttrs)).willReturn(parsed);
		given(memberRepository.findByPlatformId("12345", Platform.KAKAO))
			.willReturn(Optional.of(existing));

		// when
		Member result = oAuth2LoginService.processOAuth2User("kakao", rawAttrs);

		// then
		assertThat(result).isSameAs(existing);
		assertThat(meterRegistry.counter("oauth_login_successes").count()).isEqualTo(1.0);
	}

	@Test
	@DisplayName("신규 사용자 → 회원 등록 + 성공 카운터 1 증가")
	void registerNewMember() {
		// given
		Map<String, Object> rawAttrs = Map.of();
		Map<String, Object> parsed = Map.of(
			PLATFORM_ID,   "67890",
			EMAIL,         "new@kakao.com",
			NICKNAME,      "신규유저",
			PROFILE_IMAGE, "http://img.com"
		);

		given(kakaoProcessor.parseAttributes(rawAttrs)).willReturn(parsed);
		given(memberRepository.findByPlatformId("67890", Platform.KAKAO))
			.willReturn(Optional.empty());
		given(memberRepository.save(org.mockito.ArgumentMatchers.any(Member.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		Member result = oAuth2LoginService.processOAuth2User("kakao", rawAttrs);

		// then
		assertThat(result.getMemberProfile().getEmail()).isEqualTo("new@kakao.com");
		assertThat(result.getMemberProfile().getPlatformId()).isEqualTo("67890");
		assertThat(result.getMemberProfile().getNickname()).isEqualTo("신규유저");
		assertThat(meterRegistry.counter("oauth_login_successes").count()).isEqualTo(1.0);
	}

	@Test
	@DisplayName("지원하지 않는 플랫폼 → OAuthException")
	void throwIfUnsupportedPlatform() {
		OAuthException ex = assertThrows(
			OAuthException.class,
			() -> oAuth2LoginService.processOAuth2User("not-supported", Map.of())
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.OAUTH_UNSUPPORTED_PLATFORM);
		// 성공 카운터는 증가하지 않아야 함
		assertThat(meterRegistry.counter("oauth_login_successes").count()).isEqualTo(0.0);
	}

	@Test
	@DisplayName("platformId 누락 시 → OAuthException (성공 카운터 증가 없음)")
	void throwIfPlatformIdMissing() {
		// given
		Map<String, Object> rawAttrs = Map.of();
		Map<String, Object> invalid = Map.of(
			EMAIL,         "noid@kakao.com",
			NICKNAME,      "익명",
			PROFILE_IMAGE, "http://img.com"
		);
		given(kakaoProcessor.parseAttributes(rawAttrs)).willReturn(invalid);

		// when
		Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
			oAuth2LoginService.processOAuth2User("kakao", rawAttrs)
		);

		// then
		assertThat(thrown)
			.isInstanceOf(OAuthException.class)
			.extracting(e -> ((OAuthException) e).getErrorCode())
			.isEqualTo(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		assertThat(meterRegistry.counter("oauth_login_successes").count()).isEqualTo(0.0);
	}
}