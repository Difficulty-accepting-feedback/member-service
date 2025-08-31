package com.grow.member_service.achievement.accomplished.application.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grow.member_service.achievement.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.achievement.accomplished.application.dto.CreateAccomplishedRequest;

public interface AccomplishedApplicationService {

	AccomplishedResponse createAccomplishment(Long memberId, CreateAccomplishedRequest req);

	Page<AccomplishedResponse> searchAccomplishments(
		Long memberId,
		LocalDateTime startAt,
		LocalDateTime endAt,
		Pageable pageable
	);
}