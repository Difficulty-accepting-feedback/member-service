package com.grow.member_service.member.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.member.application.service.PhoneVerificationService;
import com.grow.member_service.member.presentation.dto.CodeRequest;
import com.grow.member_service.member.presentation.dto.PhoneRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/verification")
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "PhoneVerification", description = "전화번호 인증 API")
public class PhoneVerificationController {

	private final PhoneVerificationService phoneVerificationServiceImpl;

	/**
	 * 사용자로부터 전화번호를 받아 SMS 인증 코드를 전송합니다.
	 */
	@Operation(summary = "인증 코드 요청", description = "로그인한 회원의 전화번호로 SMS 인증 코드를 전송합니다.")
	@PostMapping("/request")
	public ResponseEntity<RsData<Long>> requestCode(
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long memberId,
		@RequestBody @Valid PhoneRequest request
	) {
		Long verificationId = phoneVerificationServiceImpl.requestVerification(
			memberId, request.getPhoneNumber()
		);
		return ResponseEntity.ok(new RsData<>(
			"200",
			"인증 코드가 전송되었습니다.",
			verificationId
		));
	}

	/**
	 * 사용자로부터 받은 인증 코드를 검증합니다.
	 */
	@Operation(summary = "인증 코드 검증", description = "로그인한 회원이 입력한 SMS 인증 코드를 검증합니다.")
	@PostMapping("/verify")
	public ResponseEntity<RsData<Void>> verifyCode(
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long memberId,
		@RequestBody @Valid CodeRequest request
	) {
		phoneVerificationServiceImpl.verifyCode(memberId, request.getCode());
		return ResponseEntity.ok(new RsData<>(
			"200",
			"인증이 완료되었습니다."
		));
	}
}