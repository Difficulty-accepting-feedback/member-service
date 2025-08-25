package com.grow.member_service.challenge.accomplished.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class Accomplished {

	private final Long accomplishedId;
	private final Long memberId;
	private final Long challengeId;
	private final LocalDateTime accomplishedAt;

	public Accomplished(
		Long memberId,
		Long challengeId,
		LocalDateTime accomplishedAt) {
		this(
			null,
			memberId,
			challengeId,
			accomplishedAt
		);
	}

	public Accomplished(
		Long accomplishedId,
		Long memberId,
		Long challengeId,
		LocalDateTime accomplishedAt
	) {
		this.accomplishedId = accomplishedId;
		this.memberId = memberId;
		this.challengeId = challengeId;
		this.accomplishedAt = accomplishedAt;
	}

	/**
	 * 비즈니스 로직 메서드
	 */
}