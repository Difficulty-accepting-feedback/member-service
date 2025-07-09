package com.grow.member_service.member.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@GetMapping("/me")
	public MemberInfoResponse getMyInfo(@AuthenticationPrincipal Long memberId) {
		return memberService.getMyInfo(memberId);
	}
}