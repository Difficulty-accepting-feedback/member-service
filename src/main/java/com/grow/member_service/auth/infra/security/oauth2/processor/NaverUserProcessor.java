package com.grow.member_service.auth.infra.security.oauth2.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

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
	public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
		@SuppressWarnings("unchecked")
		Map<String, Object> response = Optional.ofNullable(attributes.get("response"))
			.filter(m -> m instanceof Map)
			.map(m -> (Map<String, Object>) m)
			.orElseThrow(() -> new IllegalArgumentException("Invalid Naver OAuth2 attributes"));

		Map<String, Object> result = new HashMap<>();
		result.put(EMAIL_KEY, response.get("email"));
		result.put(NICKNAME_KEY, response.get("nickname"));
		result.put(PROFILE_IMAGE_KEY, response.get("profile_image"));
		result.put(PLATFORM_ID_KEY, response.get("id"));
		return result;
	}
}