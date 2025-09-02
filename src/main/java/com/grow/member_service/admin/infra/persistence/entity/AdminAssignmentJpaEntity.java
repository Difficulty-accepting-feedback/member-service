package com.grow.member_service.admin.infra.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "admin_assignment")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAssignmentJpaEntity {

	@Id
	@Column(name = "member_id")
	private Long memberId;

	@Column(nullable = false)
	private LocalDateTime grantedAt;
}