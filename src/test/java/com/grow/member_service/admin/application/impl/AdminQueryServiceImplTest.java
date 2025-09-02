package com.grow.member_service.admin.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.admin.domain.repository.AdminAssignmentRepository;

class AdminQueryServiceImplTest {

	private AdminAssignmentRepository repository;
	private AdminQueryServiceImpl service;

	@BeforeEach
	void setUp() {
		repository = mock(AdminAssignmentRepository.class);
		service = new AdminQueryServiceImpl(repository);
	}

	@Test
	@DisplayName("isAdmin: 존재하면 true")
	void isAdmin_true() {
		// given
		Long memberId = 10L;
		when(repository.existsByMemberId(memberId)).thenReturn(true);

		// when
		boolean result = service.isAdmin(memberId);

		// then
		assertThat(result).isTrue();
		verify(repository).existsByMemberId(memberId);
		verifyNoMoreInteractions(repository);
	}

	@Test
	@DisplayName("isAdmin: 없으면 false")
	void isAdmin_false() {
		// given
		Long memberId = 11L;
		when(repository.existsByMemberId(memberId)).thenReturn(false);

		// when
		boolean result = service.isAdmin(memberId);

		// then
		assertThat(result).isFalse();
		verify(repository).existsByMemberId(memberId);
		verifyNoMoreInteractions(repository);
	}
}