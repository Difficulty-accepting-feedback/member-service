package com.grow.member_service.member.infra.oauth2;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final String frontUrl = "http://localhost:3000/oauth/redirect";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest req,
		HttpServletResponse res,
		Authentication auth) throws IOException {
		String token = ((DefaultOAuth2User)auth.getPrincipal())
			.getAttributes().get("token").toString();
		String target = UriComponentsBuilder
			.fromUriString(frontUrl)
			.queryParam("token", token)
			.build().toUriString();
		getRedirectStrategy().sendRedirect(req, res, target);
	}
}