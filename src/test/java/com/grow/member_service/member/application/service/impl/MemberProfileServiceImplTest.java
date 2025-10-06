package com.grow.member_service.member.application.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceImplTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberProfileServiceImpl service;

	private Member member;

	@BeforeEach
	void setUp() {
		// 초기 점수 36.5, totalPoint 0
		Clock fixedClock = Clock.fixed(
			Instant.parse("2025-07-17T00:00:00Z"),
			ZoneId.of("UTC")
		);
		MemberProfile profile = new MemberProfile(
			"user@example.com",
			"nickname",
			"http://example.com/img.png",
			Platform.KAKAO,
			"ext-id"
		);
		MemberAdditionalInfo additionalInfo = new MemberAdditionalInfo("010-1234-5678", "Seoul");
		member = new Member(profile, additionalInfo, fixedClock);
	}

	@Test
	@DisplayName("adjustScore(): 회원이 존재하면 점수가 delta 만큼 증가하고 save 호출")
	void adjustScore_MemberExists_IncreasesScoreAndSaves() {
		// given
		long memberId = 1L;
		double delta = 2.0;
		given(memberRepository.findById(memberId))
			.willReturn(Optional.of(member));

		double beforeScore = member.getScore();

		// when
		service.adjustScore(memberId, delta);

		// then
		assertThat(member.getScore()).isEqualTo(beforeScore + delta);
		then(memberRepository).should().save(member);
	}

	@Test
	@DisplayName("adjustScore(): 회원이 없으면 MEMBER_NOT_FOUND 예외 발생")
	void adjustScore_MemberNotFound_ThrowsException() {
		// given
		long memberId = 2L;
		given(memberRepository.findById(memberId))
			.willReturn(Optional.empty());

		// when / then
		MemberException ex = assertThrows(
			MemberException.class,
			() -> service.adjustScore(memberId, 1.0)
		);
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
		then(memberRepository).should(never()).save(any());
	}
}