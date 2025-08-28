package com.grow.member_service.member.domain.service;

import java.util.Locale;
import java.util.Optional;

import com.grow.member_service.member.domain.exception.MemberDomainException;
import com.grow.member_service.member.domain.model.Member;

public interface MemberService {

	/**
	 * 주어진 닉네임이 유일하면 true, 이미 사용 중이면 false.
	 */
	boolean isNicknameUnique(String nickname);

	/**
	 * 대/소문자 무시(정규화된 키로 조회)하여 활성 회원을 찾습니다.
	 * 반환값은 Optional로, 없는 경우 빈 Optional을 반환합니다.
	 */
	Optional<Member> findActiveByNicknameIgnoreCase(String nickname);

	/**
	 * 닉네임으로 활성 회원을 반드시 얻고, 없으면 도메인 예외를 던집니다.
	 * 구현체에서 normalize 및 예외 처리를 담당합니다.
	 */
	Member getActiveByNicknameOrThrow(String nickname) throws MemberDomainException;

	/**
	 * 공백/대소문자 정규화 공통 로직
	 */
	static String normalize(String raw) {
		if (raw == null) return "";
		return raw.trim().toLowerCase(Locale.ROOT);
	}
}