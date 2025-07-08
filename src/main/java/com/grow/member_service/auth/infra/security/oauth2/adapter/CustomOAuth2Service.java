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

@Component
@RequiredArgsConstructor
public class CustomOAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
	private final OAuth2LoginService loginService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
		// OAuth 공급자에서 유저 정보 조회
		OAuth2User oauth2User = delegate.loadUser(req);

		// 어떤 플랫폼에서 로그인 했는지 식별
		String registrationId = req.getClientRegistration().getRegistrationId();

		// 가입 또는 기존 회원 조회
		Member member = loginService.processOAuth2User(registrationId, oauth2User.getAttributes());

		Map<String, Object> mapped = new HashMap<>(oauth2User.getAttributes());
		mapped.put("memberId", member.getMemberId());

		// nameAttributeKey
		String userNameAttr = req.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		return new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			mapped,
			userNameAttr
		);
	}
}