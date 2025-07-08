package com.grow.member_service.auth.application.service;

import static com.grow.member_service.auth.infra.security.oauth2.processor.KakaoUserProcessor.*;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.auth.application.dto.TokenResponse;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.service.OAuth2UserProcessor;
import com.grow.member_service.auth.infra.client.KakaoOAuthClient;
import com.grow.member_service.auth.infra.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

	private final KakaoOAuthClient kakaoClient;
	private final List<OAuth2UserProcessor> processors;
	private final MemberRepository memberRepository;
	private final JwtTokenProvider jwtProvider;

	/**
	 * 컨트롤러에서 호출할 로그인 메서드
	 */
	@Transactional
	public TokenResponse login(String registrationId, String code) {
		Member member = processOAuth2UserByCode(registrationId, code);
		String accessToken  = jwtProvider.createAccessToken(member.getMemberId());
		String refreshToken = jwtProvider.createRefreshToken(member.getMemberId());
		return new TokenResponse(accessToken, refreshToken);
	}

	/**
	 * OAuth2 필터 기반 처리(성공 핸들러 등)에서 호출할 회원 동기화 메서드
	 */
	@Transactional
	public Member processOAuth2UserByCode(String registrationId, String code) {
		Map<String,Object> rawAttrs = kakaoClient.getUserAttributesByCode(code);
		return processOAuth2User(registrationId, rawAttrs);
	}


	/**
	 * CustomOAuth2Service 에서 호출할 회원 동기화 메서드 (이미 userInfo 호출된 경우)
	 */
	@Transactional
	public Member processOAuth2User(String registrationId, Map<String,Object> rawAttrs) {
		Platform platform = Platform.valueOf(registrationId.toUpperCase());
		OAuth2UserProcessor proc = processors.stream()
			.filter(p -> p.supports(platform))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unsupported platform: " + registrationId));
		Map<String,Object> attrs = proc.parseAttributes(rawAttrs);

		String platformId = (String) attrs.get(PLATFORM_ID_KEY);
		return memberRepository.findByPlatformId(platformId, platform)
			.orElseGet(() -> registerNewMember(attrs, platform));
	}

	private Member registerNewMember(Map<String,Object> parsed, Platform platform) {
		String email    = Objects.requireNonNull((String) parsed.get(EMAIL_KEY),   "이메일이 없습니다");
		String nickname = (String) parsed.get(NICKNAME_KEY);
		String image    = (String) parsed.get(PROFILE_IMAGE_KEY);
		String pid      = (String) parsed.get(PLATFORM_ID_KEY);

		MemberProfile profile = new MemberProfile(email, nickname, image, platform, pid);
		MemberAdditionalInfo addInfo = new MemberAdditionalInfo("", "");

		return memberRepository.save(new Member(profile, addInfo, Clock.systemDefaultZone()));
	}
}