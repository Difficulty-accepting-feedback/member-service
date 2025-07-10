package com.grow.member_service.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.member.domain.model.PhoneVerification;
import com.grow.member_service.member.domain.repository.PhoneVerificationRepository;
import com.grow.member_service.member.domain.service.SmsClient;

@Service
@Transactional
public class PhoneVerificationService {
	private final PhoneVerificationRepository repository;
	private final SmsClient smsClient;

	public PhoneVerificationService(
		PhoneVerificationRepository repository,
		SmsClient smsClient
	) {
		this.repository = repository;
		this.smsClient = smsClient;
	}

	/**
	 * 소셜 가입 직후 호출되어,
	 * 도메인 모델로부터 인증 요청 객체 생성,
	 * 저장 후 반환된 도메인 객체에서 코드 획득
	 * -> SmsService 포트를 통해 SMS 전송
	 */
	public void requestVerification(Long memberId, String phoneNumber) {
		// 1) 도메인 엔티티 생성 (id=null, verified=false)
		PhoneVerification verification = PhoneVerification.newRequest(memberId, phoneNumber);
		// 2) 저장하고, 영속화된 엔티티(도메인) 복구
		PhoneVerification saved = repository.save(verification);
		// 3) SMS 전송
		smsClient.send(saved.getPhoneNumber(), "인증 코드: " + saved.getCode());
	}

	/** 사용자 입력 코드를 검증하고 상태 업데이트 */
	public void verifyCode(Long memberId, String code) {
		PhoneVerification v = repository.findByMemberId(memberId)
			.orElseThrow(() -> new IllegalArgumentException("인증 요청이 없습니다."));
		PhoneVerification verified = v.verify(code);
		repository.save(verified);
	}

	public boolean isPhoneVerified(Long memberId) {
		return repository.findByMemberId(memberId)
			.map(PhoneVerification::isVerified)
			.orElse(false);
	}
}