package com.grow.member_service.history.point.presentation.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.history.point.application.dto.PointHistoryResponse;
import com.grow.member_service.history.point.application.service.PointHistoryApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
@Tag(name = "PointHistory", description = "포인트 내역 관련 API")
@Validated
public class PointHistoryController {

	private final PointHistoryApplicationService service;

	@Operation(
		summary = "내 포인트 내역 조회",
		description = "옵션으로 기간을 파라미터로 받아 페이징/정렬하여 조회. 미입력 시 전체 내역 반환"
	)
	@GetMapping("/me")
	public ResponseEntity<RsData<Page<PointHistoryResponse>>> getMyPointHistories(
		@AuthenticationPrincipal Long memberId,

		@Parameter(description = "조회 시작일시", example = "2025-07-01T00:00:00")
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime startAt,

		@Parameter(description = "조회 종료일시", example = "2025-07-15T23:59:59")
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime endAt,

		@PageableDefault(size = 20, sort = "addAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		Page<PointHistoryResponse> page = service.searchHistories(
			memberId,
			startAt,
			endAt,
			pageable
		);
		return ResponseEntity.ok(new RsData<>("200", "내 포인트 내역 조회 성공", page));
	}
}