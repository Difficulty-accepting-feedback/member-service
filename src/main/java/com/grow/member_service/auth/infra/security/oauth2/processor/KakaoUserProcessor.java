package com.grow.member_service.auth.infra.security.oauth2.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

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
	public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
		Map<String,Object> account = cast(attributes.get("kakao_account"));
		Map<String,Object> profile = cast(account.get("profile"));

		Map<String,Object> result = new HashMap<>();
		result.put(EMAIL_KEY,   account.get("email"));
		result.put(NICKNAME_KEY, profile.get("nickname"));
		result.put(PROFILE_IMAGE_KEY, profile.get("profile_image_url"));
		result.put(PLATFORM_ID_KEY, String.valueOf(attributes.get("id")));
		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> cast(Object o) {
		return (Map<String,Object>) Optional.ofNullable(o)
			.filter(m -> m instanceof Map)
			.orElseThrow(() -> new IllegalArgumentException("Invalid OAuth2 attributes"));
	}
}