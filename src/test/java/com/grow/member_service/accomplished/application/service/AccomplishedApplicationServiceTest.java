package com.grow.member_service.accomplished.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import com.grow.member_service.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.accomplished.application.model.AccomplishedPeriod;
import com.grow.member_service.accomplished.domain.model.Accomplished;
import com.grow.member_service.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.common.exception.AccomplishedException;
import com.grow.member_service.global.exception.ErrorCode;

class AccomplishedApplicationServiceTest {

	@Mock
	private AccomplishedRepository repo;

	@InjectMocks
	private AccomplishedApplicationService service;

	private final Long MEMBER_ID = 42L;
	private final Long CHALLENGE_ID = 99L;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("새 업적 생성 성공")
	void createAccomplishment_success() {
		CreateAccomplishedRequest req = new CreateAccomplishedRequest(CHALLENGE_ID);
		// 중복 없음
		given(repo.findByMemberIdAndChallengeId(MEMBER_ID, CHALLENGE_ID))
			.willReturn(Optional.empty());

		// save 시 도메인 객체 리턴
		Accomplished savedDomain = new Accomplished(MEMBER_ID, CHALLENGE_ID, LocalDateTime.now());
		given(repo.save(any(Accomplished.class))).willReturn(savedDomain);

		AccomplishedResponse resp = service.createAccomplishment(MEMBER_ID, req);

		assertThat(resp.getChallengeId()).isEqualTo(CHALLENGE_ID);
		assertThat(resp.getAccomplishedAt()).isEqualTo(savedDomain.getAccomplishedAt());
	}

	@Test
	@DisplayName("중복 업적 생성 시 REVIEW_ALREADY_EXISTS 예외")
	void createAccomplishment_duplicateThrows() {
		CreateAccomplishedRequest req = new CreateAccomplishedRequest(CHALLENGE_ID);
		// 이미 존재하는 업적
		given(repo.findByMemberIdAndChallengeId(MEMBER_ID, CHALLENGE_ID))
			.willReturn(Optional.of(new Accomplished(MEMBER_ID, CHALLENGE_ID, LocalDateTime.now())));

		AccomplishedException ex = assertThrows(
			AccomplishedException.class,
			() -> service.createAccomplishment(MEMBER_ID, req)
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
	}

	@Test
	@DisplayName("기간 없이 조회하면 findByMemberId 호출")
	void searchAccomplishments_withoutPeriod() {
		Pageable page = PageRequest.of(0, 10, Sort.by("accomplishedAt").descending());
		// 더미 페이징 결과
		Accomplished a1 = new Accomplished(MEMBER_ID, 1L, LocalDateTime.now());
		Accomplished a2 = new Accomplished(MEMBER_ID, 2L, LocalDateTime.now().minusDays(1));
		Page<Accomplished> domainPage = new PageImpl<>(List.of(a1, a2));
		given(repo.findByMemberId(eq(MEMBER_ID), eq(page))).willReturn(domainPage);

		Page<AccomplishedResponse> respPage = service.searchAccomplishments(MEMBER_ID, null, null, page);

		assertThat(respPage.getContent())
			.extracting(AccomplishedResponse::getChallengeId)
			.containsExactlyInAnyOrder(1L, 2L);
	}

	@Test
	@DisplayName("유효 기간을 주고 조회 성공")
	void searchAccomplishments_withValidPeriod() {
		LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 31, 23, 59);
		Pageable page = PageRequest.of(0, 5);
		AccomplishedPeriod period = new AccomplishedPeriod(start, end);

		Accomplished a = new Accomplished(MEMBER_ID, 3L, start.plusDays(1));
		Page<Accomplished> domainPage = new PageImpl<>(List.of(a));
		given(repo.findByMemberIdAndAccomplishedAtBetween(
			MEMBER_ID, period.getStartAt(), period.getEndAt(), page))
			.willReturn(domainPage);

		Page<AccomplishedResponse> respPage =
			service.searchAccomplishments(MEMBER_ID, start, end, page);

		assertThat(respPage).hasSize(1);
		assertThat(respPage.getContent().get(0).getChallengeId()).isEqualTo(3L);
	}

	@Test
	@DisplayName("유효 기간 조회 결과가 없으면 ACCOMPLISHED_PERIOD_EMPTY 예외")
	void searchAccomplishments_withPeriodEmptyThrows() {
		LocalDateTime start = LocalDateTime.of(2025, 7, 1, 0, 0);
		LocalDateTime end   = LocalDateTime.of(2025, 7, 31, 23, 59);
		Pageable page = PageRequest.of(0, 5);
		AccomplishedPeriod period = new AccomplishedPeriod(start, end);

		given(repo.findByMemberIdAndAccomplishedAtBetween(
			MEMBER_ID, period.getStartAt(), period.getEndAt(), page))
			.willReturn(Page.empty());

		AccomplishedException ex = assertThrows(
			AccomplishedException.class,
			() -> service.searchAccomplishments(MEMBER_ID, start, end, page)
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOMPLISHED_PERIOD_EMPTY);
	}
}