package com.grow.member_service.member.domain.exception;

import com.grow.member_service.common.exception.DomainException;

public class MemberDomainException extends DomainException {
	public MemberDomainException(String message) {
		super(message);
	}

	public static MemberDomainException alreadyWithdrawn() {
		return new MemberDomainException("이미 탈퇴한 회원입니다.");
	}

	public static MemberDomainException negativePoints(int points) {
		return new MemberDomainException("부여할 포인트는 0 이상이어야 합니다. 입력값: " + points);
	}

	public static MemberDomainException codeNotVerified() {
		return new MemberDomainException("유효하지 않은 인증 코드입니다.");
	}

	public static MemberDomainException alreadyPhoneVerified() {
		return new MemberDomainException("이미 핸드폰 인증이 완료된 회원입니다.");
	}

	public static MemberDomainException invalidPhoneNumber() {
		return new MemberDomainException("유효하지 않은 핸드폰 번호입니다.");
	}

	public static MemberDomainException nicknameAlreadyExists(String nickname) {
		return new MemberDomainException("이미 사용 중인 닉네임입니다: " + nickname);
	}

	public static MemberDomainException notEnoughPoints(int points, int totalPoint) {
		return new MemberDomainException("포인트가 부족합니다.");
	}
}