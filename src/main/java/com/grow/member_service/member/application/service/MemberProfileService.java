package com.grow.member_service.member.application.service;

public interface MemberProfileService {
	/**
	 * 외부 서비스에서 회원 점수를 조정할 때 사용
	 */
	void adjustScore(Long memberId, double delta);
}