package com.grow.member_service.member.application.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.service.MemberProfileService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberProfileServiceImpl implements MemberProfileService {
	private final MemberRepository memberRepository;

	/**
	 * 외부 서비스에서 회원 점수를 조정할 때 사용
	 */
	@Override
	@Transactional
	public void adjustScore(Long memberId, double delta) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
		member.adjustScore(delta);
		memberRepository.save(member);
	}
}