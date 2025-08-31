package com.grow.member_service.auth.application.service.impl;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.auth.application.service.OAuth2LoginService;
import com.grow.member_service.auth.infra.security.oauth2.OAuth2AttributeKey;
import com.grow.member_service.auth.infra.security.oauth2.processor.OAuth2UserProcessor;
import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.port.NicknameGeneratorPort;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * OAuth2 로그인 처리 서비스
 */
@Service
@RequiredArgsConstructor
public class OAuth2LoginServiceImpl implements OAuth2LoginService {

	private final List<OAuth2UserProcessor> processors;
	private final MemberRepository memberRepository;
	private final NicknameGeneratorPort nicknameGenerator;
	private final Clock clock;

	/**
	 * OAuth2 로그인 후 사용자 정보를 처리하는 메서드
	 * @param registrationId ex) kakao, google, etc.
	 * @param rawAttrs provider로부터 받은 원본 사용자 정보
	 * @return 기존 회원 또는 새로 가입된 회원 엔티티
	 */
	@Override
	@Transactional
	public Member processOAuth2User(String registrationId, Map<String, Object> rawAttrs) {
		Platform platform = parsePlatform(registrationId);

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

		String platformId = extractPlatformId(attrs);
		// 기존 회원 조회
		return memberRepository.findByPlatformId(platformId, platform)
			.orElseGet(() -> registerNewMember(attrs, platform));
	}

	// 헬퍼 메서드

	/**
	 * 등록된 플랫폼 ID를 기반으로 Platform enum 파싱
	 * @param registrationId ex) kakao, google, etc.
	 * @return Platform enum
	 */
	private Platform parsePlatform(String registrationId) {
		try {
			return Platform.valueOf(registrationId.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new OAuthException(ErrorCode.OAUTH_UNSUPPORTED_PLATFORM, e);
		}
	}

	/**
	 * 파싱된 사용자 정보에서 플랫폼 ID 추출
	 * @param attrs 파싱된 사용자 정보
	 * @return 플랫폼 ID
	 */
	private String extractPlatformId(Map<String, Object> attrs) {
		String platformId = (String)attrs.get(OAuth2AttributeKey.PLATFORM_ID);
		if (platformId == null) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}
		return platformId;
	}

	/**
	 * 회원 가입 처리 (최초 로그인 시 호출)
	 * @param attrs 파싱된 사용자 정보
	 * @param platform 로그인 플랫폼 정보
	 * @return 새로 등록된 회원 엔티티
	 */
	private Member registerNewMember(Map<String, Object> attrs, Platform platform) {
		String email      = Objects.requireNonNull((String) attrs.get(OAuth2AttributeKey.EMAIL));
		String rawNickname    = (String) attrs.get(OAuth2AttributeKey.NICKNAME);
		String image      = (String) attrs.get(OAuth2AttributeKey.PROFILE_IMAGE);
		String platformId = (String) attrs.get(OAuth2AttributeKey.PLATFORM_ID);

		// 유니크 닉네임 생성
		String uniqueNickname = nicknameGenerator.generate(rawNickname);

		// MemberProfile, MemberAdditionalInfo 생성
		MemberProfile profile = new MemberProfile(
			email,
			uniqueNickname,
			image,
			platform,
			platformId
		);
		MemberAdditionalInfo addInfo = new MemberAdditionalInfo("", "");

		// Member 엔티티 생성 & 저장
		return memberRepository.save(
			new Member(profile, addInfo, clock)
		);
	}
}