package com.grow.member_service.admin.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.admin.domain.model.AdminAssignment;
import com.grow.member_service.admin.infra.persistence.entity.AdminAssignmentJpaEntity;

class AdminAssignmentMapperTest {

	@Test
	@DisplayName("toEntity: 도메인 → JPA 엔티티 매핑이 정확해야 한다")
	void toEntity_success() {
		// given
		Long memberId = 10L;
		LocalDateTime when = LocalDateTime.of(2025, 9, 2, 12, 0, 0);
		AdminAssignment domain = new AdminAssignment(memberId, when);

		// when
		AdminAssignmentJpaEntity entity = AdminAssignmentMapper.toEntity(domain);

		// then
		assertThat(entity).isNotNull();
		assertThat(entity.getMemberId()).isEqualTo(memberId);
		assertThat(entity.getGrantedAt()).isEqualTo(when);
	}

	@Test
	@DisplayName("toDomain: JPA 엔티티 → 도메인 매핑이 정확해야 한다")
	void toDomain_success() {
		// given
		Long memberId = 20L;
		LocalDateTime when = LocalDateTime.of(2025, 9, 2, 13, 30, 0);
		AdminAssignmentJpaEntity entity = new AdminAssignmentJpaEntity(memberId, when);

		// when
		AdminAssignment domain = AdminAssignmentMapper.toDomain(entity);

		// then
		assertThat(domain).isNotNull();
		assertThat(domain.getMemberId()).isEqualTo(memberId);
		assertThat(domain.getGrantedAt()).isEqualTo(when);
	}

	@Test
	@DisplayName("null 매핑: toEntity(null) / toDomain(null) 은 null을 반환해야 한다")
	void nullMapping_returnsNull() {
		assertThat(AdminAssignmentMapper.toEntity(null)).isNull();
		assertThat(AdminAssignmentMapper.toDomain(null)).isNull();
	}
}