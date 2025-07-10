package com.grow.member_service.member.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.member.application.service.PhoneVerificationService;
import com.grow.member_service.member.presentation.dto.CodeRequestDto;
import com.grow.member_service.member.presentation.dto.PhoneRequestDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class PhoneVerificationController {

	private final PhoneVerificationService phoneVerificationService;

	/**
	 * 사용자로부터 전화번호를 받아 SMS 인증 코드를 전송합니다.
	 */
	@PostMapping("/request")
	public ResponseEntity<RsData<Long>> requestCode(
		@AuthenticationPrincipal Long memberId,
		@RequestBody @Valid PhoneRequestDto request
	) {
		Long verificationId = phoneVerificationService.requestVerification(
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
	@PostMapping("/verify")
	public ResponseEntity<RsData<Void>> verifyCode(
		@AuthenticationPrincipal Long memberId,
		@RequestBody @Valid CodeRequestDto request
	) {
		phoneVerificationService.verifyCode(memberId, request.getCode());
		return ResponseEntity.ok(new RsData<>(
			"200",
			"인증이 완료되었습니다."
		));
	}
}