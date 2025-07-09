package com.grow.member_service.auth.infra.security.oauth2.processor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.grow.member_service.common.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Platform;

/**
 * 카카오 소셜 로그인 사용자 정보를 처리하는 클래스
 */
@Component
public class KakaoUserProcessor implements OAuth2UserProcessor {

	public static final String EMAIL_KEY = "email";
	public static final String PLATFORM_ID_KEY = "platformId";
	public static final String NICKNAME_KEY = "nickname";
	public static final String PROFILE_IMAGE_KEY = "profile_image";

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

		String email = (String) account.get("email");
		String nickname = (String) profile.get("nickname");
		String profileImage = (String) profile.get("profile_image");
		String id = String.valueOf(attributes.get("id"));

		if (email == null || id == null) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}

		Map<String, Object> result = new HashMap<>();
		result.put(EMAIL_KEY, email);
		result.put(NICKNAME_KEY, nickname);
		result.put(PROFILE_IMAGE_KEY, profileImage);
		result.put(PLATFORM_ID_KEY, id);

		return result;
	}
}