package com.grow.member_service.member.infra.oauth2;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.grow.member_service.member.application.dto.TokenResponse;
import com.grow.member_service.member.application.service.OAuth2LoginService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
	private final OAuth2LoginService loginService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = delegate.loadUser(req);
		String regId = req.getClientRegistration().getRegistrationId();
		String code  = req.getAdditionalParameters().get("code").toString();

		TokenResponse tokens = loginService.login(regId, code);

		return new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			Collections.singletonMap("token", tokens.getAccessToken()),
			"token"
		);
	}
}