package com.grow.member_service.auth.infra.security.oauth2.processor;

import static com.grow.member_service.auth.infra.security.oauth2.OAuth2AttributeKey.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Platform;

/**
 * 카카오 소셜 로그인 사용자 정보를 처리하는 클래스
 */
@Component
public class KakaoUserProcessor implements OAuth2UserProcessor {

	@Override
	public boolean supports(Platform platform) {
		return platform == Platform.KAKAO;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
		Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>) attributes.get("properties");

		if (account == null || profile == null) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}

		String email        = (String) account.get(EMAIL);
		String id           = String.valueOf(attributes.get("id"));
		String nickname     = (String) profile.get(NICKNAME);
		String profileImage = (String) profile.get(PROFILE_IMAGE);

		if (email == null || id == null) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}

		Map<String,Object> result = new HashMap<>();
		result.put(EMAIL,        email);
		result.put(NICKNAME,     nickname);
		result.put(PROFILE_IMAGE, profileImage);
		result.put(PLATFORM_ID,  id);
		return result;
	}
}