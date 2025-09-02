package com.grow.member_service.admin.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Getter;

@Getter
public class AdminAssignment {

	/** memberId 자체가 PK */
	private final Long memberId;
	private final LocalDateTime grantedAt;

	public AdminAssignment(Long memberId, LocalDateTime grantedAt) {
		this.memberId = Objects.requireNonNull(memberId, "멤버 ID가 필요합니다.");
		this.grantedAt = Objects.requireNonNull(grantedAt, "grantedAt이 필요합니다.");
	}
}