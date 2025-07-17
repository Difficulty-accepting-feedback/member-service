package com.grow.member_service.history.point.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.history.point.application.dto.PointHistoryResponse;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;

class PointHistoryApplicationServiceTest {

	@Mock
	private PointHistoryRepository repository;

	@InjectMocks
	private PointHistoryApplicationService service;

	private static final Long MEMBER_ID = 123L;
	private Pageable pageable;
	private ZoneId zone;
	private Clock fixedClock;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		zone = ZoneId.of("Asia/Seoul");
		Instant baseInstant = Instant.parse("2025-07-17T00:00:00Z");
		fixedClock = Clock.fixed(baseInstant, zone);

		pageable = PageRequest.of(0, 5, Sort.by("addAt").descending());
	}

	@Test
	@DisplayName("기간 없이 조회 시 repository.findByMemberId 호출 및 매핑")
	void searchHistories_withoutPeriod() {
		// given
		PointHistory ph1 = new PointHistory(1L, 100, "content1", fixedClock);
		PointHistory ph2 = new PointHistory(2L, -50, "content2", fixedClock);
		Page<PointHistory> domainPage = new PageImpl<>(List.of(ph1, ph2));
		given(repository.findByMemberId(MEMBER_ID, pageable)).willReturn(domainPage);

		// when
		Page<PointHistoryResponse> respPage =
			service.searchHistories(MEMBER_ID, null, null, pageable);

		// then
		assertThat(respPage.getTotalElements()).isEqualTo(2);
		assertThat(respPage.getContent())
			.extracting(PointHistoryResponse::getAmount)
			.containsExactlyInAnyOrder(100, -50);
		assertThat(respPage.getContent())
			.extracting(PointHistoryResponse::getContent)
			.containsExactlyInAnyOrder("content1", "content2");

		then(repository).should(never())
			.findByMemberIdAndPeriod(anyLong(), any(), any(), any());
	}

	@Test
	@DisplayName("기간을 주고 조회 성공 시 repository.findByMemberIdAndPeriod 호출 및 매핑")
	void searchHistories_withValidPeriod() {
		// given
		LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 31, 23, 59);
		LocalDateTime expectedAt = start.plusDays(5);

		Clock periodClock = Clock.fixed(expectedAt.atZone(zone).toInstant(), zone);
		PointHistory ph = new PointHistory(3L, 200, "bonus", periodClock);
		Page<PointHistory> domainPage = new PageImpl<>(List.of(ph));
		given(repository.findByMemberIdAndPeriod(MEMBER_ID, start, end, pageable))
			.willReturn(domainPage);

		// when
		Page<PointHistoryResponse> respPage =
			service.searchHistories(MEMBER_ID, start, end, pageable);

		// then
		assertThat(respPage.getTotalElements()).isEqualTo(1);
		PointHistoryResponse dto = respPage.getContent().get(0);
		assertThat(dto.getAmount()).isEqualTo(200);
		assertThat(dto.getContent()).isEqualTo("bonus");
		assertThat(dto.getAddAt()).isEqualTo(expectedAt);

		then(repository).should(never())
			.findByMemberId(MEMBER_ID, pageable);
	}

	@Test
	@DisplayName("기간 조회 시 빈 결과면 POINT_PERIOD_EMPTY 예외 발생")
	void searchHistories_withPeriodEmptyThrows() {
		// given
		LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 31, 23, 59);
		given(repository.findByMemberIdAndPeriod(MEMBER_ID, start, end, pageable))
			.willReturn(Page.empty());

		// when / then
		PointHistoryException ex = assertThrows(
			PointHistoryException.class,
			() -> service.searchHistories(MEMBER_ID, start, end, pageable)
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.POINT_PERIOD_EMPTY);
	}
}