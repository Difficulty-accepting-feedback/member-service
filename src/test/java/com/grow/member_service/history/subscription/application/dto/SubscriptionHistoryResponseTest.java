package com.grow.member_service.history.subscription.application.dto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.history.subscription.domain.model.SubscriptionHistory;
import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionStatus;

class SubscriptionHistoryResponseTest {

	@Test
	@DisplayName("fromDomain: SubscriptionHistory → SubscriptionHistoryResponse 매핑")
	void fromDomain_mapsAllFields() {
		// given
		Long historyId = 101L;
		Long memberId  = 202L;
		SubscriptionStatus status   = SubscriptionStatus.ACTIVE;
		LocalDateTime startAt  = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime endAt    = LocalDateTime.of(2025, 7, 31, 23, 59);
		LocalDateTime changeAt = LocalDateTime.of(2025, 7, 15, 12, 30);

		SubscriptionHistory domain =
			new SubscriptionHistory(historyId, memberId, status, startAt, endAt, changeAt);

		// when
		SubscriptionHistoryResponse dto = SubscriptionHistoryResponse.fromDomain(domain);

		// then
		assertThat(dto.getId()).isEqualTo(historyId);
		assertThat(dto.getStatus()).isEqualTo(status);
		assertThat(dto.getStartAt()).isEqualTo(startAt);
		assertThat(dto.getEndAt()).isEqualTo(endAt);
		assertThat(dto.getChangeAt()).isEqualTo(changeAt);
	}
}