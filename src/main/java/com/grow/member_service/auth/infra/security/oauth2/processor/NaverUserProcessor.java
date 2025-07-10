package com.grow.member_service.auth.infra.security.oauth2.processor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.grow.member_service.common.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Platform;

@Component
public class NaverUserProcessor implements OAuth2UserProcessor {

	public static final String EMAIL_KEY = "email";
	public static final String PLATFORM_ID_KEY = "platformId";
	public static final String NICKNAME_KEY = "nickname";
	public static final String PROFILE_IMAGE_KEY = "profile_image";

	@Override
	public boolean supports(Platform platform) {
		return platform == Platform.NAVER;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		if (response == null) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}

		String email = (String) response.get("email");
		String id = (String) response.get("id");
		String name = (String) response.get("name");
		String profileImage = (String) response.get("profile_image");

		if (email == null || id == null) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}

		Map<String, Object> result = new HashMap<>();
		result.put(EMAIL_KEY, email);
		result.put(NICKNAME_KEY, name);
		result.put(PROFILE_IMAGE_KEY, profileImage);
		result.put(PLATFORM_ID_KEY, id);

		return result;
	}
}