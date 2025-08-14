package com.grow.member_service.member.application.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.MemberWithdrawalLogRepository;
import com.grow.member_service.member.domain.service.MemberService;
import com.grow.member_service.member.presentation.dto.MemberUpdateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberApplicationServiceImpl implements MemberApplicationService {

	private final MemberRepository memberRepository;
	private final MemberService memberService;
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

		log.info("회원 탈퇴 처리 완료 - memberId={}, withdrawnAt={}", memberId, now);
	}

	@Transactional
	@Override
	public void updateMember(Long memberId, MemberUpdateRequest req) {
		log.info("회원 정보 업데이트 요청 수신 - memberId={}", memberId);
		log.debug("요청 본문 - nickname='{}', profileImage='{}', address='{}'",
			req.getNickname(), req.getProfileImage(), req.getAddress());

		Member m = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		boolean nicknameChanged = false;
		boolean addressChanged  = false;
		boolean imageChanged    = false;

		// 실제 값이 바뀐 경우에만 변경 (자기 자신 중복 예외 방지)
		if (req.getNickname() != null) {
			String next = req.getNickname().trim();
			String current = m.getMemberProfile().getNickname();
			if (!next.equals(current)) {
				log.info("닉네임 변경 - '{}' -> '{}'", current, next);
				m.changeNickname(next, memberService);
				nicknameChanged = true;
			} else {
				log.debug("닉네임 변경 스킵 - 동일 값('{}')", current);
			}
		}

		// 프로필 이미지는 null 포함 항상 덮어쓰기
		if (!Objects.equals(req.getProfileImage(), m.getMemberProfile().getProfileImage())) {
			log.info("프로필 이미지 변경 - '{}' -> '{}'",
				m.getMemberProfile().getProfileImage(), req.getProfileImage());
			m.changeProfileImage(req.getProfileImage());
			imageChanged = true;
		} else {
			log.debug("프로필 이미지 변경 스킵 - 동일 값");
		}

		// 주소-> 값이 들어왔고 실제로 바뀐 경우에만 변경 (구 단위 문자열)
		if (req.getAddress() != null) {
			String nextAddr = req.getAddress().trim();
			String currAddr = m.getAdditionalInfo().getAddress();
			if (!Objects.equals(nextAddr, currAddr)) {
				log.info("주소 변경 - '{}' -> '{}'", currAddr, nextAddr);
				m.changeAddress(nextAddr);
				addressChanged = true;
			} else {
				log.debug("주소 변경 스킵 - 동일 값");
			}
		}

		memberRepository.save(m);
		log.info("회원 정보 업데이트 완료 - memberId={}, changed[nickname={}, image={}, address={}]",
			memberId, nicknameChanged, imageChanged, addressChanged);
	}

	private Member findById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
	}

	@Transactional
	@Override
	public void toggleMatching(Long memberId, boolean enabled) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		if (member.isMatchingEnabled() == enabled) {
			log.debug("매칭 기능 변경 스킵 - 이미 동일 상태, memberId={}, enabled={}", memberId, enabled);
			return;
		}

		if (enabled) {
			member.enableMatching();
		} else {
			member.disableMatching();
		}

		memberRepository.save(member);
		log.info("매칭 기능 상태 변경 - memberId={}, enabled={}", memberId, enabled);
	}
}