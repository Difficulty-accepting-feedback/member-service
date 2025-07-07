package com.grow.member_service.member.application.service;

import static com.grow.member_service.member.infra.oauth2.KakaoUserProcessor.*;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.member.application.dto.TokenResponse;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.service.OAuth2UserProcessor;
import com.grow.member_service.member.infra.client.KakaoOAuthClient;
import com.grow.member_service.member.infra.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

	private final KakaoOAuthClient kakaoClient;
	private final List<OAuth2UserProcessor> processors;
	private final MemberRepository memberRepository;
	private final JwtTokenProvider jwtProvider;

	@Transactional
	public TokenResponse login(String registrationId, String code) {
		// 1) 토큰 발급 & OAuth2User attributes 조회
		Map<String,Object> rawAttrs = kakaoClient.getUserAttributesByCode(code);
		// 2) 플랫폼별 파싱
		Platform platform = Platform.valueOf(registrationId.toUpperCase());
		OAuth2UserProcessor proc = processors.stream()
			.filter(p -> p.supports(platform))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unsupported platform"));
		Map<String,Object> attrs = proc.parseAttributes(rawAttrs);

		// 3) 가입 또는 조회
		String platformId = (String) attrs.get(PLATFORM_ID_KEY);
		Member member = memberRepository.findByPlatformId(platformId, platform)
			.orElseGet(() -> registerNew(attrs, platform));

		// 4) JWT 발급
		return new TokenResponse(
			jwtProvider.createAccessToken(member.getMemberId()),
			jwtProvider.createRefreshToken(member.getMemberId())
		);
	}

	private Member registerNew(Map<String,Object> parsed, Platform platform) {
		// 이메일·닉네임 등 필수 값 검증
		String email    = Objects.requireNonNull((String) parsed.get("email"),
			"이메일이 없습니다");
		String nickname = (String) parsed.get("nickname");
		String image    = (String) parsed.get("profileImage");
		String pid      = (String) parsed.get("platformId");

		MemberProfile profile = new MemberProfile(
			email, nickname, image, platform, pid
		);
		// 기본 값 없으면 빈 문자열로 초기화
		MemberAdditionalInfo addInfo = new MemberAdditionalInfo("", "");

		Member newMember = new Member(profile, addInfo, Clock.systemDefaultZone());
		return memberRepository.save(newMember);
	}
}