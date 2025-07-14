package com.grow.member_service.auth.infra.security.oauth2;

/**
 * OAuth2 User 정보 파싱 시 사용되는 공통 키 정의
 */
public final class OAuth2AttributeKey {
	private OAuth2AttributeKey() {

	}

	// 공통
	public static final String EMAIL           = "email";
	public static final String PLATFORM_ID     = "platformId";
	public static final String NICKNAME        = "nickname";
	public static final String PROFILE_IMAGE   = "profile_image";

	// Naver
	public static final String NAVER_RESPONSE  = "response";

	// Kakao
	public static final String KAKAO_ACCOUNT   = "kakao_account";
	public static final String KAKAO_PROFILE   = "properties";

	// Google
	public static final String GOOGLE_SUB      = "sub";
	public static final String GOOGLE_PICTURE  = "picture";
	public static final String GOOGLE_NAME     = "name";
}