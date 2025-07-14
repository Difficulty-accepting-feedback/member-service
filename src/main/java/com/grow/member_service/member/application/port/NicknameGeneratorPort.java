package com.grow.member_service.member.application.port;

public interface NicknameGeneratorPort {
	/**
	 * @param base 사용자가 지정한 기본 닉네임
	 * @return 생성된 유니크 닉네임
	 */
	String generate(String base);
}