package com.grow.member_service.member.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.application.service.MemberApplicationService;

import lombok.RequiredArgsConstructor;

/**
 * [임시 내부 API]
 * - 목적: 게이트웨이가 없어서 payment-service(8081) → member-service(8080) 간
 * 서버-서버 통신을 안전하게 허용하기 위한 내부 전용 조회 엔드포인트.
 * (게이트웨이 도입 시 제거 예정)
 */
@RestController
@RequestMapping("/internal/members")
@RequiredArgsConstructor
public class MemberInternalController {

	private final MemberApplicationService memberApplicationService;

	@GetMapping("/{memberId}")
	public RsData<MemberInfoResponse> getById(@PathVariable Long memberId) {
		MemberInfoResponse info = memberApplicationService.getMyInfo(memberId);
		return new RsData<>("200", "OK", info);
	}
}