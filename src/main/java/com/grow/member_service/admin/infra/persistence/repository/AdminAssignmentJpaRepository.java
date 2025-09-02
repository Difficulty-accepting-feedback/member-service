package com.grow.member_service.admin.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.admin.infra.persistence.entity.AdminAssignmentJpaEntity;

public interface AdminAssignmentJpaRepository extends JpaRepository<AdminAssignmentJpaEntity, Long> {
	void deleteByMemberId(Long memberId);
	boolean existsByMemberId(Long memberId);
}