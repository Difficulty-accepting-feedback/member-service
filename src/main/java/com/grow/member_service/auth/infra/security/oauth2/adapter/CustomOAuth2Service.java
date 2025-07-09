package com.grow.member_service.auth.infra.security.oauth2.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.grow.member_service.auth.application.service.OAuth2LoginService;
import com.grow.member_service.member.domain.model.Member;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security의 OAuth2 로그인 과정 중 사용자 정보를 처리하는 서비스
 * - 실제 유저 정보를 가져오고,
 * - 해당 정보를 기반으로 로그인/회원가입 처리
 */
@Component
@RequiredArgsConstructor
public class CustomOAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
	private final OAuth2LoginService loginService;

	/**
	 * OAuth2 로그인 성공 후 사용자 정보를 가져오고, 우리 시스템의 회원 정보와 연결함
	 * @param req OAuth2UserRequest: 토큰과 client 정보 등을 포함한 요청
	 * @return OAuth2User: Security Context에 저장될 사용자 정보 객체
	 */
	@Override
	public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
		// access token을 사용해서 provider로부터 사용자 정보 조회
		OAuth2User oauth2User = delegate.loadUser(req);

		// OAuth 플랫폼 정보 확인
		String registrationId = req.getClientRegistration().getRegistrationId();

		// 회원 가입 or 기존 회원 조회 및 반환
		Member member = loginService.processOAuth2User(registrationId, oauth2User.getAttributes());

		// 원본 provider attributes + 우리 시스템의 memberId를 합쳐서 사용자 정보 구성
		Map<String, Object> mapped = new HashMap<>(oauth2User.getAttributes());
		mapped.put("memberId", member.getMemberId());

		// provider에서 사용하는 사용자 이름 속성
		String userNameAttr = req.getClientRegistration()
			.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		// DefaultOAuth2User를 사용하여 Security Context에 저장할 사용자 정보 객체 생성
		return new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			mapped,
			userNameAttr
		);
	}
}