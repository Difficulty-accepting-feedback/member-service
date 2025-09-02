package com.grow.member_service.admin.domain.repository;

import com.grow.member_service.admin.domain.model.AdminAssignment;

public interface AdminAssignmentRepository {
	AdminAssignment save(AdminAssignment assignment);
	void deleteByMemberId(Long memberId);
	boolean existsByMemberId(Long memberId);
}