package com.grow.member_service.auth.infra.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KakaoOAuthClient {
	private final RestTemplate restTemplate;
	private final String clientId;
	private final String clientSecret;
	private final String redirectUri;

	public KakaoOAuthClient(RestTemplate restTemplate,
		@Value("${oauth.kakao.client-id}") String clientId,
		@Value("${oauth.kakao.client-secret}") String clientSecret,
		@Value("${oauth.kakao.redirect-uri}") String redirectUri
	) {
		this.restTemplate   = restTemplate;
		this.clientId       = clientId;
		this.clientSecret   = clientSecret;
		this.redirectUri    = redirectUri;
	}

	public String getAccessToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// client_secret 파라미터 추가!
		String body = "grant_type=authorization_code"
			+ "&client_id="     + clientId
			+ "&client_secret=" + clientSecret
			+ "&redirect_uri="  + redirectUri
			+ "&code="          + code;

		HttpEntity<String> request = new HttpEntity<>(body, headers);
		ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
			"https://kauth.kakao.com/oauth/token",
			request,
			KakaoTokenResponse.class
		);
		return response.getBody().getAccessToken();
	}

	@SuppressWarnings("unchecked")
	public Map<String,Object> getUserAttributes(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> req = new HttpEntity<>(headers);
		ResponseEntity<Map> resp = restTemplate.exchange(
			"https://kapi.kakao.com/v2/user/me",
			HttpMethod.GET, req, Map.class);
		return resp.getBody();
	}

	public Map<String,Object> getUserAttributesByCode(String code) {
		String token = getAccessToken(code);
		return getUserAttributes(token);
	}
}