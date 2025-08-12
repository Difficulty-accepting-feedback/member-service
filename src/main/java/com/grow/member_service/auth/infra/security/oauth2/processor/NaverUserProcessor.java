package com.grow.member_service.auth.infra.security.oauth2.processor;

import static com.grow.member_service.auth.infra.security.oauth2.OAuth2AttributeKey.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.grow.member_service.common.exception.OAuthException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.enums.Platform;

@Component
public class NaverUserProcessor implements OAuth2UserProcessor {

	@Override
	public boolean supports(Platform platform) {
		return platform == Platform.NAVER;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> parseAttributes(Map<String, Object> attributes) {
		Object resObj = attributes.get("response");
		if (!(resObj instanceof Map)) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}
		Map<String, Object> response = (Map<String, Object>) resObj;

		// 네이버는 id가 문자열로 내려옴
		String id = toStr(response.get("id"));
		if (id == null || id.isBlank()) {
			throw new OAuthException(ErrorCode.OAUTH_INVALID_ATTRIBUTE);
		}

		// 선택 동의 항목은 null 허용
		String email        = toStr(response.get("email"));
		String nickname     = firstNonBlank(toStr(response.get("nickname")), toStr(response.get("name")));
		String profileImage = toStr(response.get("profile_image"));

		Map<String, Object> result = new HashMap<>();
		result.put(PLATFORM_ID,   id);          // 필수
		result.put(EMAIL,         email);       // 선택
		result.put(NICKNAME,      nickname);    // 선택
		result.put(PROFILE_IMAGE, profileImage);// 선택
		return result;
	}

	private String toStr(Object v) { return v == null ? null : String.valueOf(v); }
	private String firstNonBlank(String a, String b) {
		if (a != null && !a.isBlank()) return a;
		if (b != null && !b.isBlank()) return b;
		return null;
	}
}