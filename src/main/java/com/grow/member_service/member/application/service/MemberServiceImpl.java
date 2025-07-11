package com.grow.member_service.member.application.service;

import org.springframework.stereotype.Service;

import com.grow.member_service.auth.application.service.OAuth2LoginService;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final OAuth2LoginService oauth2LoginService;
	private final MemberRepository memberRepository;

	@Override
	public MemberInfoResponse getMyInfo(Long memberId) {
		return memberRepository.findById(memberId)
			.map(MemberInfoResponse::from)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
	}
}