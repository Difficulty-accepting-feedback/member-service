package com.grow.member_service.admin.application.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.admin.application.AdminQueryService;
import com.grow.member_service.admin.domain.repository.AdminAssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminQueryServiceImpl implements AdminQueryService {

	private final AdminAssignmentRepository repository;

	/**
	 * 관리자 여부 조회
	 * 추후에 다른 서비스에서 관리자 여부를 조회할 수 있으므로 별도의 서비스로 분리
	 * @param memberId 회원 ID
	 * @return 관리자 여부
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean isAdmin(Long memberId) {
		boolean result = repository.existsByMemberId(memberId);
		log.debug("[멤버][관리자] 관리자 여부 조회 - memberId={}, result={}", memberId, result);
		return result;
	}
}