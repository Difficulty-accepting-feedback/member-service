package com.grow.member_service.admin.application.impl;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.admin.application.AdminCommandService;
import com.grow.member_service.admin.domain.model.AdminAssignment;
import com.grow.member_service.admin.domain.repository.AdminAssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCommandServiceImpl implements AdminCommandService {

	private final AdminAssignmentRepository repository;
	private final Clock clock;

	/**
	 * 관리자 권한 부여
	 * @param memberId 관리자 권한을 부여할 회원 ID
	 * @return 관리자 권한이 부여된 회원 ID
	 */
	@Override
	@Transactional
	public Long grant(Long memberId) {
		// 이미 관리자면 그대로 종료
		if (repository.existsByMemberId(memberId)) {
			log.info("[MEMBER][ADMIN] 이미 관리자 권한을 가진 회원 - memberId={}", memberId);
			return memberId;
		}
		AdminAssignment saved = repository.save(
			new AdminAssignment(memberId, LocalDateTime.now(clock))
		);
		log.info("[MEMBER][ADMIN] 관리자 권한 부여 완료 - memberId={}", memberId);
		return saved.getMemberId();
	}

	/**
	 * 관리자 권한 회수
	 * @param memberId 관리자 권한을 회수할 회원 ID
	 */
	@Override
	@Transactional
	public void revoke(Long memberId) {
		// 존재하지 않아도 예외 없이 성공 처리
		repository.deleteByMemberId(memberId);
		log.info("[MEMBER][ADMIN] 관리자 권한 해제 완료 - memberId={}", memberId);
	}
}