package com.grow.member_service.member.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NearbyMemberResponse(
	Long memberId,
	String nickname,
	String region,
	Double distanceKm
) {}