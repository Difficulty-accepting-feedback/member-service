package com.grow.member_service.member.infra.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberServiceImpl memberService;

	private Member sampleMember;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(
			Instant.parse("2025-07-17T00:00:00Z"),
			ZoneOffset.UTC
		);
		sampleMember = new Member(
			new MemberProfile(
				"user@example.com",
				"nickname",
				"http://example.com/img.png",
				Platform.KAKAO,
				"ext-123"
			),
			new MemberAdditionalInfo("010-1234-5678", "Seoul"),
			fixedClock
		);
	}

	@Test
	@DisplayName("isNicknameUnique(): 저장된 회원이 없으면 true 반환")
	void isNicknameUnique_whenNoMemberFound_returnsTrue() {
		// given
		String nickname = "uniqueNick";
		given(memberRepository.findByNickname(nickname)).willReturn(Optional.empty());

		// when
		boolean result = memberService.isNicknameUnique(nickname);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isNicknameUnique(): 저장된 회원이 있으면 false 반환")
	void isNicknameUnique_whenMemberFound_returnsFalse() {
		// given
		String nickname = "existingNick";
		given(memberRepository.findByNickname(nickname)).willReturn(Optional.of(sampleMember));

		// when
		boolean result = memberService.isNicknameUnique(nickname);

		// then
		assertThat(result).isFalse();
	}
}