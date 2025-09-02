package com.grow.member_service.admin.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.admin.application.AdminCommandService;
import com.grow.member_service.admin.application.AdminQueryService;
import com.grow.member_service.global.dto.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 권한 관리 API")
public class AdminController {

	private final AdminCommandService commandService;
	private final AdminQueryService queryService;

	// 내부용
	@Operation(summary = "관리자 승급")
	@PostMapping("/internal/v1/admin/members/{memberId}")
	public ResponseEntity<RsData<Long>> grant(@PathVariable Long memberId) {
		Long id = commandService.grant(memberId);
		return ResponseEntity.ok(new RsData<>("200", "관리자 권한 부여 완료", id));
	}

	@Operation(summary = "관리자 해제")
	@DeleteMapping("/internal/v1/admin/members/{memberId}")
	public ResponseEntity<RsData<Void>> revoke(@PathVariable Long memberId) {
		commandService.revoke(memberId);
		return ResponseEntity.ok(new RsData<>("200", "관리자 권한 해제 완료", null));
	}

	// 외부 서비스용
	@Operation(summary = "관리자 여부 조회", description = "다른 서비스에서 권한 검증 시 사용")
	@GetMapping("/api/v1/admin/members/{memberId}/is-admin")
	public ResponseEntity<RsData<Boolean>> isAdmin(@PathVariable Long memberId) {
		boolean result = queryService.isAdmin(memberId);
		return ResponseEntity.ok(new RsData<>("200", "관리자 여부 조회 성공", result));
	}
}