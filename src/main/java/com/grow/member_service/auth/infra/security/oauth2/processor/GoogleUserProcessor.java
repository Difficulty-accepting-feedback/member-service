package com.grow.member_service.auth.infra.security.oauth2.processor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

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
		Map<String, Object> result = new HashMap<>();
		result.put(EMAIL_KEY, attributes.get("email"));
		result.put(NICKNAME_KEY, attributes.get("name"));
		result.put(PROFILE_IMAGE_KEY, attributes.get("picture"));
		result.put(PLATFORM_ID_KEY, attributes.get("sub")); // Google의 고유 사용자 ID
		return result;
	}
}