package com.grow.member_service.admin.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.grow.member_service.admin.domain.model.AdminAssignment;
import com.grow.member_service.admin.domain.repository.AdminAssignmentRepository;

class AdminCommandServiceImplTest {

	private AdminAssignmentRepository repository;
	private Clock fixedClock;
	private AdminCommandServiceImpl service;

	@BeforeEach
	void setUp() {
		repository = mock(AdminAssignmentRepository.class);
		fixedClock = Clock.fixed(Instant.parse("2025-09-02T12:00:00Z"), ZoneOffset.UTC);
		service = new AdminCommandServiceImpl(repository, fixedClock);
	}

	@Test
	@DisplayName("이미 관리자면 저장 없이 memberId 반환(멱등)")
	void grant_whenAlreadyAdmin_thenNoSaveAndReturnMemberId() {
		// given
		Long memberId = 100L;
		when(repository.existsByMemberId(memberId)).thenReturn(true);

		// when
		Long result = service.grant(memberId);

		// then
		assertThat(result).isEqualTo(memberId);
		verify(repository, never()).save(any(AdminAssignment.class));
		verify(repository).existsByMemberId(memberId);
		verifyNoMoreInteractions(repository);
	}

	@Test
	@DisplayName("관리자 아님 → 저장하고 memberId 반환, grantedAt은 고정 시각")
	void grant_whenNotAdmin_thenSaveAndReturnMemberId() {
		// given
		Long memberId = 200L;
		when(repository.existsByMemberId(memberId)).thenReturn(false);
		// save 동작 시 도메인 객체 그대로 반환되도록 설정
		when(repository.save(any(AdminAssignment.class)))
			.thenAnswer(invocation -> invocation.getArgument(0, AdminAssignment.class));

		// when
		Long result = service.grant(memberId);

		// then
		assertThat(result).isEqualTo(memberId);

		// save 인자로 들어온 도메인 검증 (grantedAt == fixed now)
		ArgumentCaptor<AdminAssignment> captor = ArgumentCaptor.forClass(AdminAssignment.class);
		verify(repository).save(captor.capture());
		AdminAssignment saved = captor.getValue();

		assertThat(saved.getMemberId()).isEqualTo(memberId);
		assertThat(saved.getGrantedAt())
			.isEqualTo(LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone()));

		verify(repository).existsByMemberId(memberId);
		verifyNoMoreInteractions(repository);
	}

	@Test
	@DisplayName("revoke는 존재 여부와 관계 없이 deleteByMemberId 호출(멱등)")
	void revoke_alwaysDeletesOnce() {
		// given
		Long memberId = 300L;

		// when
		service.revoke(memberId);

		// then
		verify(repository).deleteByMemberId(memberId);
		verifyNoMoreInteractions(repository);
	}
}