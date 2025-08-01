package com.grow.member_service.auth.infra.security.oauth2.processor;

import java.util.Map;

import com.grow.member_service.member.domain.model.enums.Platform;

public interface OAuth2UserProcessor {
	boolean supports(Platform platform);
	//공통 Map(email, nickname, profileImage, platformId) 으로 변환
	Map<String,Object> parseAttributes(Map<String,Object> attributes);
}