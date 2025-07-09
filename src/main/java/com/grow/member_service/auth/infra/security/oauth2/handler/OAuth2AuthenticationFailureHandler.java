package com.grow.member_service.auth.infra.security.oauth2.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2 인증 실패 시 리다이렉트 핸들러
 * 지정된 URL로 리다이렉트 (변경 필요)
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	private final String frontUrl = "http://localhost:3000/oauth/redirect";

	@Override
	public void onAuthenticationFailure(HttpServletRequest req,
		HttpServletResponse res,
		AuthenticationException ex) throws IOException {
		String target = UriComponentsBuilder
			.fromUriString(frontUrl)
			.queryParam("error", ex.getMessage())
			.build().toUriString();
		getRedirectStrategy().sendRedirect(req, res, target);
	}
}