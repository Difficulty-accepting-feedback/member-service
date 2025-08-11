package com.grow.member_service.member.presentation.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.application.service.MemberApplicationService;
import com.grow.member_service.member.presentation.dto.MatchingToggleRequest;
import com.grow.member_service.member.presentation.dto.MemberUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name= "Member", description = "회원 관련 API")
public class MemberController {
	private final MemberApplicationService memberApplicationService;

	@Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
	@GetMapping("/me")
	public ResponseEntity<RsData<MemberInfoResponse>> getMyInfo(
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long memberId
	) {
		MemberInfoResponse info = memberApplicationService.getMyInfo(memberId);
		return ResponseEntity.ok(
			new RsData<>("200", "내 정보 조회 성공", info)
		);
	}

	@Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 탈퇴합니다. (soft-delete 처리)")
	@DeleteMapping("/withdraw")
	public ResponseEntity<RsData<Void>> withdraw(
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long memberId
	) {
		memberApplicationService.withdraw(memberId);
		return ResponseEntity.ok(
			new RsData<>("200", "회원 탈퇴 성공", null)
		);
	}

	@Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
	@PatchMapping("/me")
	public ResponseEntity<RsData<Void>> updateMember(
		@AuthenticationPrincipal Long memberId,
		@RequestBody MemberUpdateRequest req
	) {
		memberApplicationService.updateMember(memberId, req);
		return ResponseEntity.ok(new RsData<>("200", "회원 정보 수정 성공", null));
	}

	@PatchMapping("/me/matching")
	@Operation(summary = "매칭 기능 활성화/비활성화")
	public ResponseEntity<RsData<Void>> toggleMatching(
		@AuthenticationPrincipal Long memberId,
		@RequestBody @Valid MatchingToggleRequest req
	) {
		memberApplicationService.toggleMatching(memberId, req.getIsEnabled());
		return ResponseEntity.ok(new RsData<>("200", "매칭 설정 변경 성공", null));
	}

	@Operation(summary = "로그아웃", description = "로그아웃 처리 및 쿠키 삭제")
	@PostMapping("/logout")
	public ResponseEntity<RsData<Void>> logout() {
		ResponseCookie a = ResponseCookie.from("access_token", "")
			.httpOnly(true).secure(true).path("/").maxAge(0).sameSite("None").build();
		ResponseCookie r = ResponseCookie.from("refresh_token", "")
			.httpOnly(true).secure(true).path("/").maxAge(0).sameSite("None").build();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, a.toString())
			.header(HttpHeaders.SET_COOKIE, r.toString())
			.body(new RsData<>("200", "로그아웃 되었습니다.", null));
	}
}