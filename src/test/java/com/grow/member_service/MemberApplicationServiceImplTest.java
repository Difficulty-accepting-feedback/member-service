package com.grow.member_service.member.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.domain.exception.MemberDomainException;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.MemberWithdrawalLogRepository;
import com.grow.member_service.member.domain.service.MemberService;
import com.grow.member_service.member.presentation.dto.MemberUpdateRequest;

@ExtendWith(MockitoExtension.class)
class MemberApplicationServiceImplTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private MemberWithdrawalLogRepository withdrawalLogRepository;

	@InjectMocks
	private MemberApplicationServiceImpl service;

	private Member sampleMember;

	@BeforeEach
	void setUp() {
		// create a Member with fixed creation time
		Clock fixedClock = Clock.fixed(
			Instant.parse("2025-01-01T00:00:00Z"),
			ZoneOffset.UTC
		);
		sampleMember = new Member(
			// MemberProfile(email, nickname, profileImage, platform, platformId)
			new com.grow.member_service.member.domain.model.MemberProfile(
				"user@example.com",
				"origNick",
				"http://example.com/orig.png",
				com.grow.member_service.member.domain.model.Platform.KAKAO,
				"ext-id"
			),
			new com.grow.member_service.member.domain.model.MemberAdditionalInfo(
				"01012345678",
				"Seoul"
			),
			fixedClock
		);
	}

	@Test
	@DisplayName("getMyInfo(): 회원이 존재하면 MemberInfoResponse 반환")
	void getMyInfo_Found_ReturnsResponse() {
		Long memberId = 1L;
		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(sampleMember));

		MemberInfoResponse resp = service.getMyInfo(memberId);

		assertNotNull(resp, "MemberInfoResponse가 반환되어야 한다");
		verify(memberRepository).findById(memberId);
	}

	@Test
	@DisplayName("getMyInfo(): 회원이 존재하지 않으면 MemberException 발생")
	void getMyInfo_NotFound_ThrowsMemberException() {
		when(memberRepository.findById(anyLong()))
			.thenReturn(Optional.empty());

		assertThrows(MemberException.class,
			() -> service.getMyInfo(99L),
			"회원이 없으면 MemberException을 던져야 한다");
	}

	@Test
	@DisplayName("withdraw(): 회원이 존재하면 로그 저장 후 탈퇴 처리 및 저장 호출")
	void withdraw_Found_LogsAndWithdraws() {
		Long memberId = 2L;
		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(sampleMember));

		service.withdraw(memberId);

		// 로그 저장
		verify(withdrawalLogRepository).saveFromMember(sampleMember);
		// 도메인 탈퇴 처리
		assertTrue(sampleMember.isWithdrawn(), "withdraw 호출 후 isWithdrawn가 true여야 한다");
		// 영속화 호출
		verify(memberRepository).save(sampleMember);
	}

	@Test
	@DisplayName("withdraw(): 회원이 존재하지 않으면 MemberException 발생")
	void withdraw_NotFound_ThrowsMemberException() {
		when(memberRepository.findById(anyLong()))
			.thenReturn(Optional.empty());

		assertThrows(MemberException.class,
			() -> service.withdraw(123L),
			"회원이 없으면 MemberException을 던져야 한다");
		verifyNoInteractions(withdrawalLogRepository);
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("updateMember(): 회원이 존재하지 않으면 MemberException 발생")
	void updateMember_NotFound_ThrowsMemberException() {
		when(memberRepository.findById(anyLong()))
			.thenReturn(Optional.empty());

		MemberUpdateRequest req = new MemberUpdateRequest("nick", "img", "addr");
		assertThrows(MemberException.class,
			() -> service.updateMember(5L, req),
			"회원이 없으면 MemberException을 던져야 한다");
	}

	@Test
	@DisplayName("updateMember(): 모든 필드가 주어지면 각 변경 메서드 호출 후 저장")
	void updateMember_AllFields_UpdatesAndSaves() {
		Long memberId = 3L;
		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(sampleMember));
		when(memberService.isNicknameUnique("newNick"))
			.thenReturn(true);

		MemberUpdateRequest req = new MemberUpdateRequest("newNick", "newImg", "newAddr");
		service.updateMember(memberId, req);

		verify(memberService).isNicknameUnique("newNick");
		assertEquals("newNick", sampleMember.getMemberProfile().getNickname());
		assertEquals("newImg", sampleMember.getMemberProfile().getProfileImage());
		assertEquals("newAddr", sampleMember.getAdditionalInfo().getAddress());
		verify(memberRepository).save(sampleMember);
	}

	@Test
	@DisplayName("updateMember(): nickname 중복 시 MemberDomainException 발생하고 저장 안 함")
	void updateMember_DuplicateNickname_ThrowsDomainException() {
		Long memberId = 4L;
		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(sampleMember));
		when(memberService.isNicknameUnique("dupNick"))
			.thenReturn(false);

		MemberUpdateRequest req = new MemberUpdateRequest("dupNick", "img", null);

		MemberDomainException ex = assertThrows(
			MemberDomainException.class,
			() -> service.updateMember(memberId, req),
			"닉네임 중복 시 MemberDomainException을 던져야 한다"
		);
		assertTrue(ex.getMessage().contains("dupNick"));
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("updateMember(): nickname null 이면 changeNickname 미호출")
	void updateMember_NullNickname_SkipsNickname() {
		Long memberId = 5L;
		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(sampleMember));

		MemberUpdateRequest req = new MemberUpdateRequest(null, "imgOnly", null);
		service.updateMember(memberId, req);

		// 닉네임 변경 호출되지 않음
		assertNotEquals("imgOnly", sampleMember.getMemberProfile().getNickname());
		// 프로필 이미지는 항상 변경
		assertEquals("imgOnly", sampleMember.getMemberProfile().getProfileImage());
		verify(memberRepository).save(sampleMember);
	}

	@Test
	@DisplayName("updateMember(): address null 이면 changeAddress 미호출")
	void updateMember_NullAddress_SkipsAddress() {
		Long memberId = 6L;
		when(memberRepository.findById(memberId))
			.thenReturn(Optional.of(sampleMember));
		when(memberService.isNicknameUnique("okNick"))
			.thenReturn(true);

		MemberUpdateRequest req = new MemberUpdateRequest("okNick", "img", null);
		service.updateMember(memberId, req);

		assertEquals("okNick", sampleMember.getMemberProfile().getNickname());
		assertEquals("img", sampleMember.getMemberProfile().getProfileImage());
		// 주소는 변경되지 않음
		assertEquals("Seoul", sampleMember.getAdditionalInfo().getAddress());
		verify(memberRepository).save(sampleMember);
	}
}