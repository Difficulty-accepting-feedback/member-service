package com.grow.member_service.challenge.challenge.domain.model;

import lombok.Getter;

@Getter
public class Challenge {
	private final Long challengeId;
	private final String name; // 업적 이름
	private final String description; // 업적 설명
	private final int point; // 보상 포인트

	public Challenge(Long id, String name, String description, int point) {
		this.challengeId = id;
		this.name = name;
		this.description = description;
		this.point = point;
	}
}