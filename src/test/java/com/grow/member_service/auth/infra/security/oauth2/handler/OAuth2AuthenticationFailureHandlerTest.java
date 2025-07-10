package com.grow.member_service.auth.infra.security.oauth2.handler;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import com.grow.member_service.auth.infra.config.OAuthProperties;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

	@Mock
	private OAuthProperties oauthProperties;

	private OAuth2AuthenticationFailureHandler handler;

	@BeforeEach
	void setUp() {
		// 테스트용 리디렉션 URI 세팅
		given(oauthProperties.getRedirectUri())
			.willReturn("http://localhost:3000/oauth/redirect");
		handler = new OAuth2AuthenticationFailureHandler(oauthProperties);
	}

	@Test
	@DisplayName("인증 실패 시 frontUrl에 error 파라미터를 붙여 리다이렉트")
	void onAuthenticationFailure_appendsErrorParamAndRedirects() throws Exception {
		MockHttpServletRequest request  = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		AuthenticationException ex = new AuthenticationException("bad credentials") {};

		handler.onAuthenticationFailure(request, response, ex);

		assertThat(response.getRedirectedUrl())
			.isEqualTo("http://localhost:3000/oauth/redirect?error=bad credentials");
	}
}