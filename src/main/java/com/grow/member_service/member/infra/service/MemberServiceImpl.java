package com.grow.member_service.member.infra.service;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.service.MemberService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;

	/**
	 * 주어진 닉네임이 유일하면 true, 이미 사용 중이면 false.
	 * 입력값이 공백이거나 null인 경우에는 예외 발생.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean isNicknameUnique(String nickname) {
		String key = MemberService.normalize(nickname);
		if (key.isEmpty()) {
			throw new MemberException(ErrorCode.NICKNAME_INVALID);
		}
		return memberRepository.findByNickname(key).isEmpty();
	}

	/**
	 * 대/소문자 무시(정규화된 키로 조회)하여 활성 회원을 찾습니다.
	 * 반환값은 Optional로, 없는 경우 빈 Optional을 반환합니다.
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Member> findActiveByNicknameIgnoreCase(String nickname) {
		String key = MemberService.normalize(nickname);
		if (key.isEmpty()) return Optional.empty(); // 조회용 API는 빈 키에 대해 비어있음 반환
		return memberRepository.findActiveByNicknameIgnoreCase(key);
	}

	/**
	 * 닉네임으로 활성 회원을 반드시 얻고, 없으면 도메인 예외를 던집니다.
	 */
	@Override
	@Transactional(readOnly = true)
	public Member getActiveByNicknameOrThrow(String nickname) {
		String key = MemberService.normalize(nickname);
		if (key.isEmpty()) {
			throw new MemberException(ErrorCode.NICKNAME_INVALID);
		}
		return findActiveByNicknameIgnoreCase(key)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
	}
}