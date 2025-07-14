package com.grow.member_service.member.infra.service;

import org.springframework.stereotype.Component;

import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.service.MemberService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;

	@Override
	public boolean isNicknameUnique(String nickname) {
		return memberRepository.findByNickname(nickname).isEmpty();
	}
}