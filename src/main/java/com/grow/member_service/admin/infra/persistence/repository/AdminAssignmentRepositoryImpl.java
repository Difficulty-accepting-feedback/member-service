package com.grow.member_service.admin.infra.persistence.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.admin.domain.model.AdminAssignment;
import com.grow.member_service.admin.domain.repository.AdminAssignmentRepository;
import com.grow.member_service.admin.infra.persistence.mapper.AdminAssignmentMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminAssignmentRepositoryImpl implements AdminAssignmentRepository {

	private final AdminAssignmentJpaRepository jpa;

	@Override
	@Transactional
	public AdminAssignment save(AdminAssignment assignment) {
		return AdminAssignmentMapper.toDomain(jpa.save(AdminAssignmentMapper.toEntity(assignment)));
	}

	/**
	 * 관리자 권한 부여 이력 삭제
	 */
	@Override
	@Transactional
	public void deleteByMemberId(Long memberId) {
		jpa.deleteByMemberId(memberId);
	}

	/**
	 * 관리자 권한 부여 이력이 존재하는지 여부
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existsByMemberId(Long memberId) {
		return jpa.existsByMemberId(memberId);
	}
}