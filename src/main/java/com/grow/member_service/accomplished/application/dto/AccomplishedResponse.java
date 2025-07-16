package com.grow.member_service.accomplished.application.dto;

import java.time.LocalDateTime;

import com.grow.member_service.accomplished.domain.model.Accomplished;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccomplishedResponse {

	private Long accomplishedId;
	private Long challengeId;
	private LocalDateTime accomplishedAt;

	public static AccomplishedResponse from(Accomplished d) {
		return AccomplishedResponse.builder()
			.accomplishedId(d.getAccomplishedId())
			.challengeId(d.getChallengeId())
			.accomplishedAt(d.getAccomplishedAt())
			.build();
	}
}