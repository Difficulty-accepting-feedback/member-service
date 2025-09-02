package com.grow.member_service.admin.infra.persistence.mapper;

import com.grow.member_service.admin.domain.model.AdminAssignment;
import com.grow.member_service.admin.infra.persistence.entity.AdminAssignmentJpaEntity;

public class AdminAssignmentMapper {

	public static AdminAssignment toDomain(AdminAssignmentJpaEntity e) {
		if (e == null) {
			return null;
		}
		return new AdminAssignment(e.getMemberId(), e.getGrantedAt());
	}

	public static AdminAssignmentJpaEntity toEntity(AdminAssignment d) {
		if (d == null) {
			return null;
		}
		return new AdminAssignmentJpaEntity(d.getMemberId(), d.getGrantedAt());
	}
}