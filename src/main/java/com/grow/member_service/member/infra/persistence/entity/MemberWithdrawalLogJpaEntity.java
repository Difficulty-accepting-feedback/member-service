package com.grow.member_service.member.infra.persistence.entity;

import java.time.LocalDateTime;

import com.grow.member_service.member.domain.model.enums.Platform;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "member_withdrawal_log")
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)

public class MemberWithdrawalLogJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long memberId;
	private String email;
	private String nickname;

	@Enumerated(EnumType.STRING)
	private Platform platform;

	private String platformId;
	private String phoneNumber;
	private LocalDateTime withdrawnAt;
}