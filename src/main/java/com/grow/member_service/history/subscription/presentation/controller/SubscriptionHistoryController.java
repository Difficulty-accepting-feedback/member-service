package com.grow.member_service.history.subscription.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.history.subscription.application.dto.SubscriptionHistoryResponse;
import com.grow.member_service.history.subscription.application.service.SubscriptionHistoryApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscription", description = "구독 이력 관련 API")
public class SubscriptionHistoryController {

	private final SubscriptionHistoryApplicationService subscriptionHistoryApplicationService;

	@Operation(summary = "내 구독 이력 조회", description = "인증된 멤버의 모든 구독 이력을 반환합니다.")
	@GetMapping("/me")
	public ResponseEntity<RsData<List<SubscriptionHistoryResponse>>> getMySubscriptions(
		@AuthenticationPrincipal Long memberId
	) {
		List<SubscriptionHistoryResponse> list = subscriptionHistoryApplicationService.getMySubscriptionHistories(memberId);
		return ResponseEntity.ok(new RsData<>("200", "내 구독 이력 조회 성공", list));
	}
}