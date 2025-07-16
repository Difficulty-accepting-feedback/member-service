package com.grow.member_service.history.subscription.application.dto;

import java.time.LocalDateTime;

import com.grow.member_service.history.subscription.domain.model.SubscriptionHistory;
import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionHistoryResponse {

	private Long id;
	private SubscriptionStatus status;
	private LocalDateTime startAt;
	private LocalDateTime endAt;
	private LocalDateTime changeAt;

	public static SubscriptionHistoryResponse fromDomain(SubscriptionHistory d) {
		return SubscriptionHistoryResponse.builder()
			.id(d.getSubscriptionHistoryId())
			.status(d.getSubscriptionStatus())
			.startAt(d.getStartAt())
			.endAt(d.getEndAt())
			.changeAt(d.getChangeAt())
			.build();
	}
}