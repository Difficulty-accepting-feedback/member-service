package com.grow.member_service.auth.infra.security.oauth2.processor;

import static com.grow.member_service.auth.infra.security.oauth2.OAuth2AttributeKey.*;
import static com.grow.member_service.global.exception.ErrorCode.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.member.domain.model.Platform;

@Component
public class GoogleUserProcessor implements OAuth2UserProcessor {

	@Override
	public boolean supports(Platform platform) {
		return platform == Platform.GOOGLE;
	}

	@Override
	public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
		String email        = (String) attributes.get(EMAIL);
		String id           = (String) attributes.get(GOOGLE_SUB);
		String nickname     = (String) attributes.get(GOOGLE_NAME);
		String profileImage = (String) attributes.get(GOOGLE_PICTURE);

		if (email == null || id == null) {
			throw new OAuthException(OAUTH_INVALID_ATTRIBUTE);
		}

		Map<String,Object> result = new HashMap<>();
		result.put(EMAIL,        email);
		result.put(NICKNAME,     nickname);
		result.put(PROFILE_IMAGE, profileImage);
		result.put(PLATFORM_ID,  id);
		return result;
	}
}