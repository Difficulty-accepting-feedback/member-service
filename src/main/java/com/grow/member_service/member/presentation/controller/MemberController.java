package com.grow.member_service.member.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	/**
	 * 내 정보 조회 API
	 */
	@GetMapping("/me")
	public ResponseEntity<RsData<MemberInfoResponse>> getMyInfo(
		@AuthenticationPrincipal Long memberId
	) {
		MemberInfoResponse info = memberService.getMyInfo(memberId);
		return ResponseEntity.ok(
			new RsData<>("200", "내 정보 조회 성공", info)
		);
	}

	/**
	 * 내 계정 탈퇴 (soft-delete)
	 */
	@DeleteMapping("/withdraw")
	public ResponseEntity<RsData<Void>> withdraw(
		@AuthenticationPrincipal Long memberId
	) {
		memberService.withdraw(memberId);
		return ResponseEntity.ok(
			new RsData<>("200", "회원 탈퇴 성공", null)
		);
	}
}