package com.grow.member_service.member.domain.service;

public interface MemberService {
	/**
	 * 주어진 닉네임이 유일하면 true, 이미 사용 중이면 false
	 */
	boolean isNicknameUnique(String nickname);
}