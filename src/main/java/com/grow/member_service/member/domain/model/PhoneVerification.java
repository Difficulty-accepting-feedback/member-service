package com.grow.member_service.member.domain.model;

import java.time.Instant;

import lombok.Getter;

@Getter
public class PhoneVerification {
	private final Long id;
	private final Long memberId;
	private final String phoneNumber;
	private final String code;
	private final Instant createdAt;
	private final boolean verified;

	public PhoneVerification(
		Long id,
		Long memberId,
		String phoneNumber,
		String code,
		Instant createdAt,
		boolean verified
	) {
		this.id = id;
		this.memberId = memberId;
		this.phoneNumber = phoneNumber;
		this.code = code;
		this.createdAt = createdAt;
		this.verified = verified;
	}

	/** 신규 인증 요청 생성 (id=null, verified=false) */
	public static PhoneVerification newRequest(Long memberId, String phoneNumber) {
		String code = String.format("%06d", (int)(Math.random() * 1_000_000));
		return new PhoneVerification(
			null,
			memberId,
			phoneNumber,
			code,
			Instant.now(),
			false
		);
	}

	/** 인증 코드 검증 후, verified=true 상태의 새 인스턴스 반환 */
	public PhoneVerification verify(String inputCode) {
		if (!this.code.equals(inputCode)) {
			throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
		}
		return new PhoneVerification(
			this.id,
			this.memberId,
			this.phoneNumber,
			this.code,
			this.createdAt,
			true
		);
	}
}