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

/**
 * 카카오 OAuth 서버와 통신, 인가 코드를 액세스 토큰으로 교환하고,
 * 액세스 토큰으로 사용자 정보 조회하는 클라이언트
 */
@Component
public class KakaoOAuthClient {
	private final RestTemplate restTemplate;
	private final String clientId;
	private final String clientSecret;
	private final String redirectUri;

	/**
	 * @param restTemplate Http 요청을 보낼 RestTemplate
	 * @param clientId Kakao OAuth 클라이언트 ID
	 * @param clientSecret Kakao OAuth 클라이언트 시크릿
	 * @param redirectUri Kakao OAuth 리다이렉트 URI
	 */
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

	/**
	 * 인가 코드로 액세스 토큰을 요청
	 * @param code 인가 코드
	 * @return 액세스 토큰
	 */
	public String getAccessToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// 요청 바디에 필요한 파라미터 설정
		String body = "grant_type=authorization_code"
			+ "&client_id="     + clientId
			+ "&client_secret=" + clientSecret
			+ "&redirect_uri="  + redirectUri
			+ "&code="          + code;

		HttpEntity<String> request = new HttpEntity<>(body, headers);
		// Post 요청 보내고, 매핑
		ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
			"https://kauth.kakao.com/oauth/token",
			request,
			KakaoTokenResponse.class
		);
		return response.getBody().getAccessToken();
	}

	/**
	 * 액세스 토큰으로 사용자 정보를 조회
	 * @param accessToken 액세스 토큰
	 * @return 사용자 정보 맵
	 */
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