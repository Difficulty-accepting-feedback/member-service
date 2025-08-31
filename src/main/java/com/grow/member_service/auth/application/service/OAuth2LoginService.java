package com.grow.member_service.auth.application.service;

import java.util.Map;

import com.grow.member_service.member.domain.model.Member;

public interface OAuth2LoginService {
	Member processOAuth2User(String registrationId, Map<String, Object> rawAttrs);
}