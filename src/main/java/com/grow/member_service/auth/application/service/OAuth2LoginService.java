package com.grow.member_service.auth.application.service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.auth.infra.security.oauth2.processor.KakaoUserProcessor;
import com.grow.member_service.auth.infra.security.oauth2.processor.OAuth2UserProcessor;
import com.grow.member_service.common.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * OAuth2 로그인 처리 서비스
 */
@Service
@RequiredArgsConstructor
public class OAuth2LoginService {
	private final List<OAuth2UserProcessor> processors;
	private final MemberRepository memberRepository;

	/**
	 * OAuth2 로그인 후 사용자 정보를 처리하는 메서드
	 * @param registrationId ex) kakao, google, etc.
	 * @param rawAttrs provider로부터 받은 원본 사용자 정보
	 * @return 기존 회원 또는 새로 가입된 회원 엔티티
	 */
	@Transactional
	public Member processOAuth2User(String registrationId, Map<String, Object> rawAttrs) {
		Platform platform;
		try {
			platform = Platform.valueOf(registrationId.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new OAuthException(ErrorCode.OAUTH_UNSUPPORTED_PLATFORM);
		}

		// 지원하는 플랫폼인지 확인하고, 해당 플랫폼에 맞는 프로세서 찾기
		OAuth2UserProcessor processor = processors.stream()
			.filter(p -> p.supports(platform))
			.findFirst()
			.orElseThrow(() -> new OAuthException(ErrorCode.OAUTH_UNSUPPORTED_PLATFORM));

		// 사용자 정보 파싱
		Map<String, Object> attrs;
		try {
			attrs = processor.parseAttributes(rawAttrs);
		} catch (Exception e) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE, e);
		}

		String platformId = (String) attrs.get(KakaoUserProcessor.PLATFORM_ID_KEY);

		if (platformId == null) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}
		// 기존 회원 조회
		return memberRepository.findByPlatformId(platformId, platform)
			.orElseGet(() -> registerNewMember(attrs, platform));
	}

	/**
	 * 회원 가입 처리 (최초 로그인 시 호출)
	 * @param parsed 파싱된 사용자 정보
	 * @param platform 로그인 플랫폼 정보
	 * @return 새로 등록된 회원 엔티티
	 */
	private Member registerNewMember(Map<String,Object> parsed, Platform platform) {
		String email    = Objects.requireNonNull((String) parsed.get(KakaoUserProcessor.EMAIL_KEY));
		String nickname = (String) parsed.get(KakaoUserProcessor.NICKNAME_KEY);
		String image    = (String) parsed.get(KakaoUserProcessor.PROFILE_IMAGE_KEY);
		String pid      = (String) parsed.get(KakaoUserProcessor.PLATFORM_ID_KEY);

		MemberProfile profile = new MemberProfile(email, nickname, image, platform, pid);
		MemberAdditionalInfo addInfo = new MemberAdditionalInfo("", "");

		return memberRepository.save(new Member(profile, addInfo, Clock.systemDefaultZone()));
	}
}