package com.grow.member_service.achievement.accomplished.presentation.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.achievement.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.achievement.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.achievement.accomplished.application.service.AccomplishedApplicationService;
import com.grow.member_service.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/accomplished")
@Tag(name = "Accomplished", description = "업적 내역 관련 API")
@Validated
public class AccomplishedController {

	private final AccomplishedApplicationService accomplishedApplicationService;

	@Operation(summary = "새 업적 달성", description = "challengeId를 받아 해당 업적을 새로 달성 처리")
	@PostMapping("/me")
	public ResponseEntity<RsData<AccomplishedResponse>> create(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody CreateAccomplishedRequest req
	) {
		AccomplishedResponse res = accomplishedApplicationService.createAccomplishment(memberId, req);
		return ResponseEntity.ok(new RsData<>("201", "업적 달성 성공", res));
	}

	@Operation(summary = "내가 달성한 업적 조회", description = "옵션으로 기간을 파라미터로 받아 페이징/정렬하여 조회")
	@GetMapping("/me")
	public ResponseEntity<RsData<Page<AccomplishedResponse>>> search(
		@AuthenticationPrincipal Long memberId,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime startAt,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime endAt,

		@PageableDefault(size = 20, sort = "accomplishedAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		Page<AccomplishedResponse> page = accomplishedApplicationService.searchAccomplishments(memberId, startAt, endAt, pageable);
		return ResponseEntity.ok(new RsData<>("200", "내 업적 조회 성공", page));
	}
}