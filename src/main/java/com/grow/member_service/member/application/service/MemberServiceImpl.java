package com.grow.member_service.member.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.MemberWithdrawalLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;
	private final MemberWithdrawalLogRepository withdrawalLogRepository;


	@Override
	public MemberInfoResponse getMyInfo(Long memberId) {
		return memberRepository.findById(memberId)
			.map(MemberInfoResponse::from)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
	}

	@Override
	@Transactional
	public void withdraw(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		UUID uuid = UUID.randomUUID();
		LocalDateTime now = LocalDateTime.now();

		// 로그 저장
		withdrawalLogRepository.saveFromMember(member);

		// 마스킹 및 탈퇴 처리
		member.markAsWithdrawn(uuid);
		memberRepository.save(member);
	}
}