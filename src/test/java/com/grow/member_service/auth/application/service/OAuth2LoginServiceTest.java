package com.grow.member_service.auth.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grow.member_service.auth.infra.security.oauth2.processor.KakaoUserProcessor;
import com.grow.member_service.auth.infra.security.oauth2.processor.OAuth2UserProcessor;
import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginServiceTest {

	@InjectMocks
	private OAuth2LoginService oAuth2LoginService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private OAuth2UserProcessor kakaoProcessor;

	@BeforeEach
	void init() {
		lenient().when(kakaoProcessor.supports(Platform.KAKAO)).thenReturn(true);
		oAuth2LoginService = new OAuth2LoginService(List.of(kakaoProcessor), memberRepository);
	}

	@Test
	@DisplayName("기존 사용자 존재 -> 회원 그대로 반환")
	void findExistingMember() {
		// given
		Map<String, Object> rawAttrs = Map.of();
		Map<String, Object> parsedAttrs = Map.of(
			KakaoUserProcessor.PLATFORM_ID_KEY, "12345",
			KakaoUserProcessor.EMAIL_KEY, "test@kakao.com",
			KakaoUserProcessor.NICKNAME_KEY, "장무영",
			KakaoUserProcessor.PROFILE_IMAGE_KEY, "http://img.com"
		);

		Member existing = new Member(
			new MemberProfile("test@kakao.com", "장무영", "http://img.com", Platform.KAKAO, "12345"),
			new MemberAdditionalInfo("", ""),
			Clock.systemUTC()
		);

		given(kakaoProcessor.parseAttributes(rawAttrs)).willReturn(parsedAttrs);
		given(memberRepository.findByPlatformId("12345", Platform.KAKAO)).willReturn(Optional.of(existing));

		// when
		Member result = oAuth2LoginService.processOAuth2User("kakao", rawAttrs);

		// then
		assertThat(result).isEqualTo(existing);
	}

	@Test
	@DisplayName("신규 사용자-> 회원 등록")
	void registerNewMember() {
		// given
		Map<String, Object> rawAttrs = Map.of();
		Map<String, Object> parsedAttrs = Map.of(
			KakaoUserProcessor.PLATFORM_ID_KEY, "67890",
			KakaoUserProcessor.EMAIL_KEY, "new@kakao.com",
			KakaoUserProcessor.NICKNAME_KEY, "신규유저",
			KakaoUserProcessor.PROFILE_IMAGE_KEY, "http://img.com"
		);

		given(kakaoProcessor.parseAttributes(rawAttrs)).willReturn(parsedAttrs);
		given(memberRepository.findByPlatformId("67890", Platform.KAKAO)).willReturn(Optional.empty());
		given(memberRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

		// when
		Member result = oAuth2LoginService.processOAuth2User("kakao", rawAttrs);

		// then
		assertThat(result.getMemberProfile().getEmail()).isEqualTo("new@kakao.com");
		assertThat(result.getMemberProfile().getPlatformId()).isEqualTo("67890");
	}

	@Test
	@DisplayName("지원하지 않는 플랫폼-> OAuthException 발생")
	void throwIfUnsupportedPlatform() {
		// when
		Throwable thrown = catchThrowable(() ->
			oAuth2LoginService.processOAuth2User("not-supported", Map.of())
		);

		// then
		assertThat(thrown)
			.isInstanceOf(OAuthException.class)
			.extracting(e -> ((OAuthException) e).getErrorCode())
			.isEqualTo(ErrorCode.OAUTH_UNSUPPORTED_PLATFORM);
	}

	@Test
	@DisplayName("플랫폼 ID가 누락되면 OAuthException이 발생한다")
	void throwIfPlatformIdMissing() {
		// given
		Map<String, Object> rawAttrs = Map.of(); // 원본 입력
		Map<String, Object> invalidParsed = Map.of( // platformId 없음
			KakaoUserProcessor.EMAIL_KEY, "noid@kakao.com",
			KakaoUserProcessor.NICKNAME_KEY, "익명",
			KakaoUserProcessor.PROFILE_IMAGE_KEY, "http://img.com"
		);

		given(kakaoProcessor.parseAttributes(rawAttrs)).willReturn(invalidParsed);

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