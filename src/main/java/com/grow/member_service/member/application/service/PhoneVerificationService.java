package com.grow.member_service.member.application.service;

public interface PhoneVerificationService {

	/**
	 * 소셜 가입 직후 인증 요청 생성 및 SMS 발송
	 * @return 생성된 PhoneVerification ID
	 */
	Long requestVerification(Long memberId, String phoneNumber);

	/**
	 * 사용자 입력 코드 검증 및 멤버 인증 처리
	 */
	void verifyCode(Long memberId, String code);

	/**
	 * 인증 여부 조회
	 */
	boolean isPhoneVerified(Long memberId);
}