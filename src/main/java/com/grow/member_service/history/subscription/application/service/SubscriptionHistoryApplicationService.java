package com.grow.member_service.history.subscription.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.SubscriptionHistoryException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.history.subscription.application.dto.SubscriptionHistoryResponse;
import com.grow.member_service.history.subscription.domain.repository.SubscriptionHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionHistoryApplicationService {

	private final SubscriptionHistoryRepository repository;

	/**
	 * 해당 멤버의 모든 구독 이력 조회
	 */
	@Transactional(readOnly = true)
	public List<SubscriptionHistoryResponse> getMySubscriptionHistories(Long memberId) {
		List<SubscriptionHistoryResponse> list = repository.findByMemberId(memberId).stream()
			.map(SubscriptionHistoryResponse::fromDomain)
			.collect(Collectors.toList());

		if (list.isEmpty()) {
			throw new SubscriptionHistoryException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
		}

		return list;
	}
}