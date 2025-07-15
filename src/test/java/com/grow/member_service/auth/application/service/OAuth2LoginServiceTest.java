package com.grow.member_service.auth.application.service;

import static com.grow.member_service.auth.infra.security.oauth2.OAuth2AttributeKey.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.anyString;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grow.member_service.auth.infra.security.oauth2.processor.OAuth2UserProcessor;
import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.port.NicknameGeneratorPort;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginServiceTest {

	private OAuth2LoginService oAuth2LoginService;

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private OAuth2UserProcessor kakaoProcessor;
	@Mock
	private NicknameGeneratorPort nicknameGenerator;

	private Clock fixedClock;

	@BeforeEach
	void init() {
		fixedClock = Clock.fixed(Instant.parse("2025-07-10T00:00:00Z"), ZoneId.of("UTC"));

		lenient().when(kakaoProcessor.supports(Platform.KAKAO)).thenReturn(true);

		lenient().when(nicknameGenerator.generate(anyString()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		oAuth2LoginService = new OAuth2LoginService(
			List.of(kakaoProcessor),
			memberRepository,
			nicknameGenerator,
			fixedClock
		);
	}

	@Test
	@DisplayName("기존 사용자 존재 → 그대로 반환")
	void findExistingMember() {
		// given
		Map<String, Object> rawAttrs = Map.of();
		Map<String, Object> parsed = Map.of(
			PLATFORM_ID,    "12345",
			EMAIL,          "test@kakao.com",
			NICKNAME,       "장무영",
			PROFILE_IMAGE,  "http://img.com"
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
	}

	@Test
	@DisplayName("신규 사용자 → 회원 등록")
	void registerNewMember() {
		// given
		Map<String, Object> rawAttrs = Map.of();
		Map<String, Object> parsed = Map.of(
			PLATFORM_ID,    "67890",
			EMAIL,          "new@kakao.com",
			NICKNAME,       "신규유저",
			PROFILE_IMAGE,  "http://img.com"
		);

		given(kakaoProcessor.parseAttributes(rawAttrs)).willReturn(parsed);
		given(memberRepository.findByPlatformId("67890", Platform.KAKAO))
			.willReturn(Optional.empty());
		given(memberRepository.save(any(Member.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		Member result = oAuth2LoginService.processOAuth2User("kakao", rawAttrs);

		// then
		assertThat(result.getMemberProfile().getEmail()).isEqualTo("new@kakao.com");
		assertThat(result.getMemberProfile().getPlatformId()).isEqualTo("67890");
		assertThat(result.getMemberProfile().getNickname()).isEqualTo("신규유저");
	}

	@Test
	@DisplayName("지원하지 않는 플랫폼 → OAuthException")
	void throwIfUnsupportedPlatform() {
		// when & then
		OAuthException ex = assertThrows(
			OAuthException.class,
			() -> oAuth2LoginService.processOAuth2User("not-supported", Map.of())
		);

		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.OAUTH_UNSUPPORTED_PLATFORM);
	}

	@Test
	@DisplayName("platformId 누락 시 → OAuthException")
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
		Throwable thrown = catchThrowable(() ->
			oAuth2LoginService.processOAuth2User("kakao", rawAttrs)
		);

		// then
		assertThat(thrown)
			.isInstanceOf(OAuthException.class)
			.extracting(e -> ((OAuthException) e).getErrorCode())
			.isEqualTo(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
	}
}