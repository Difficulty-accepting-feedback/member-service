package com.grow.member_service.auth.infra.security.oauth2.processor;

import static com.grow.member_service.auth.infra.security.oauth2.OAuth2AttributeKey.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Platform;

@Component
public class NaverUserProcessor implements OAuth2UserProcessor {

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

		String email        = (String) response.get(EMAIL);
		String id           = (String) response.get(PLATFORM_ID);
		String nickname     = (String) response.get(NICKNAME);
		String profileImage = (String) response.get(PROFILE_IMAGE);

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