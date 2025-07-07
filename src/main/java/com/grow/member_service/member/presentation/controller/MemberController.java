package com.grow.member_service.member.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.application.dto.TokenResponse;
import com.grow.member_service.member.application.service.MemberService;
import com.grow.member_service.member.application.service.OAuth2LoginService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	private final OAuth2LoginService loginService;

	@GetMapping("/oauth/{provider}")
	public TokenResponse oauthLogin(@PathVariable String provider,
		@RequestParam String code) {
		return loginService.login(provider, code);
	}

	@GetMapping("/me")
	public MemberInfoResponse getMyInfo(@AuthenticationPrincipal Long memberId) {
		return memberService.getMyInfo(memberId);
	}
}