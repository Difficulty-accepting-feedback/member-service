package com.grow.member_service.history.subscription.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.grow.member_service.common.exception.SubscriptionHistoryException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.history.subscription.application.dto.SubscriptionHistoryResponse;
import com.grow.member_service.history.subscription.domain.model.SubscriptionHistory;
import com.grow.member_service.history.subscription.domain.repository.SubscriptionHistoryRepository;
import com.grow.member_service.history.subscription.infra.persistence.entity.SubscriptionStatus;

class SubscriptionHistoryApplicationServiceTest {

	@Mock
	private SubscriptionHistoryRepository repository;

	@InjectMocks
	private SubscriptionHistoryApplicationService service;

	private static final Long MEMBER_ID = 777L;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("구독 이력 존재 시, DTO 리스트로 매핑되어 반환된다")
	void getMySubscriptionHistories_withData() {
		// given
		LocalDateTime start1  = LocalDateTime.of(2025,7,1,0,0);
		LocalDateTime end1    = LocalDateTime.of(2025,7,31,23,59);
		LocalDateTime change1 = LocalDateTime.of(2025,7,15,12,0);
		// 도메인 생성: (historyId, memberId, status, startAt, endAt, changeAt)
		SubscriptionHistory h1 = new SubscriptionHistory(
			10L, MEMBER_ID, SubscriptionStatus.ACTIVE, start1, end1, change1);
		LocalDateTime start2  = LocalDateTime.of(2025,8,1,0,0);
		LocalDateTime end2    = LocalDateTime.of(2025,8,31,23,59);
		LocalDateTime change2 = LocalDateTime.of(2025,8,15,12,0);
		SubscriptionHistory h2 = new SubscriptionHistory(
			11L, MEMBER_ID, SubscriptionStatus.CANCELED, start2, end2, change2);

		given(repository.findByMemberId(MEMBER_ID))
			.willReturn(List.of(h1, h2));

		// when
		List<SubscriptionHistoryResponse> resp = service.getMySubscriptionHistories(MEMBER_ID);

		// then
		assertThat(resp).hasSize(2);

		// 첫 번째 DTO 검증
		SubscriptionHistoryResponse dto1 = resp.get(0);
		assertThat(dto1.getId()).isEqualTo(10L);
		assertThat(dto1.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(dto1.getStartAt()).isEqualTo(start1);
		assertThat(dto1.getEndAt()).isEqualTo(end1);
		assertThat(dto1.getChangeAt()).isEqualTo(change1);

		// 두 번째 DTO 검증
		SubscriptionHistoryResponse dto2 = resp.get(1);
		assertThat(dto2.getId()).isEqualTo(11L);
		assertThat(dto2.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
		assertThat(dto2.getStartAt()).isEqualTo(start2);
		assertThat(dto2.getEndAt()).isEqualTo(end2);
		assertThat(dto2.getChangeAt()).isEqualTo(change2);
	}

	@Test
	@DisplayName("구독 이력 없으면 SubscriptionHistoryException(SUBSCRIPTION_NOT_FOUND) 발생")
	void getMySubscriptionHistories_emptyThrows() {
		// given
		given(repository.findByMemberId(MEMBER_ID)).willReturn(List.of());

		// when / then
		SubscriptionHistoryException ex = assertThrows(
			SubscriptionHistoryException.class,
			() -> service.getMySubscriptionHistories(MEMBER_ID)
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SUBSCRIPTION_NOT_FOUND);
	}
}