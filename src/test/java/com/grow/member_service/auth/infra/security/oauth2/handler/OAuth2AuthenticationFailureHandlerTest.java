package com.grow.member_service.auth.infra.security.oauth2.handler;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

class OAuth2AuthenticationFailureHandlerTest {

	private final OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();

	@Test
	@DisplayName("인증 실패 시 frontUrl에 error 파라미터를 붙여 리다이렉트")
	void onAuthenticationFailure_appendsErrorParamAndRedirects() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		AuthenticationException ex = new AuthenticationException("bad credentials") {};

		handler.onAuthenticationFailure(request, response, ex);

		assertThat(response.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect?error=bad credentials");
	}
}