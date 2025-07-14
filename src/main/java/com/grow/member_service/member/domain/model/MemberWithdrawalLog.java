package com.grow.member_service.member.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberWithdrawalLog {
	private final Long memberId;
	private final String originalEmail;
	private final String originalNickname;
	private final Platform platform;
	private final String originalPlatformId;
	private final String originalPhoneNumber;
	private final LocalDateTime withdrawnAt;
}