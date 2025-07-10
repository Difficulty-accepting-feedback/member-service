package com.grow.member_service.auth.infra.security.oauth2.processor;

import static com.grow.member_service.global.exception.ErrorCode.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.grow.member_service.common.OAuthException;
import com.grow.member_service.member.domain.model.Platform;

@Component
public class GoogleUserProcessor implements OAuth2UserProcessor {

	public static final String EMAIL_KEY = "email";
	public static final String PLATFORM_ID_KEY = "platformId";
	public static final String NICKNAME_KEY = "nickname";
	public static final String PROFILE_IMAGE_KEY = "profile_image";

	@Override
	public boolean supports(Platform platform) {
		return platform == Platform.GOOGLE;
	}

	@Override
	public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
		String email = (String) attributes.get("email");
		String name = (String) attributes.get("name");
		String picture = (String) attributes.get("picture");
		String sub = (String) attributes.get("sub"); // 고유 사용자 ID

		if (email == null || sub == null) {
			throw new OAuthException(OAUTH_INVALID_ATTRIBUTE);
		}

		Map<String, Object> result = new HashMap<>();
		result.put(EMAIL_KEY, email);
		result.put(NICKNAME_KEY, name);
		result.put(PROFILE_IMAGE_KEY, picture);
		result.put(PLATFORM_ID_KEY, sub);

		return result;
	}
}