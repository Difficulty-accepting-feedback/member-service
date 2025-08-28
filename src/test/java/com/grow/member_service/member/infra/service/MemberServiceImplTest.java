package com.grow.member_service.member.infra.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
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
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.service.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberServiceImpl memberService;

	private MemberProfile profile;
	private MemberAdditionalInfo additionalInfo;
	private Clock fixedClock;
	private LocalDateTime fixedNow;

	@BeforeEach
	void setUp() {
		fixedNow = LocalDateTime.ofInstant(Instant.parse("2025-07-11T00:00:00Z"), ZoneOffset.UTC);
		fixedClock = Clock.fixed(fixedNow.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

		profile = new MemberProfile(
			"user@example.com",
			"nickname",
			"http://example.com/img.png",
			Platform.KAKAO,
			"external-id-123"
		);
		additionalInfo = new MemberAdditionalInfo("01012345678", "Seoul");
	}

	@Test
	@DisplayName("isNicknameUnique: 리포지토리에 닉네임이 없으면 true 반환 (normalize 적용)")
	void isNicknameUnique_WhenNotFound_ReturnsTrue() {
		// given
		String raw = "someNick ";
		String normalized = MemberService.normalize(raw);

		when(memberRepository.findByNickname(normalized)).thenReturn(Optional.empty());

		// when
		boolean unique = memberService.isNicknameUnique(raw);

		// then
		assertTrue(unique);
		verify(memberRepository, times(1)).findByNickname(normalized);
	}

	@Test
	@DisplayName("isNicknameUnique: 리포지토리에 닉네임이 있으면 false 반환 (normalize 적용)")
	void isNicknameUnique_WhenFound_ReturnsFalse() {
		// given
		String raw = "ExistingNick ";
		String normalized = MemberService.normalize(raw);

		Member member = new Member(profile, additionalInfo, fixedClock);
		when(memberRepository.findByNickname(normalized)).thenReturn(Optional.of(member));

		// when
		boolean unique = memberService.isNicknameUnique(raw);

		// then
		assertFalse(unique);
		verify(memberRepository, times(1)).findByNickname(normalized);
	}

	@Test
	@DisplayName("isNicknameUnique: null 또는 공백 입력이면 MemberException(NICKNAME_INVALID) 발생")
	void isNicknameUnique_NullOrBlank_Throws() {
		// null
		MemberException exNull = assertThrows(MemberException.class, () -> memberService.isNicknameUnique(null));
		assertEquals(ErrorCode.NICKNAME_INVALID, exNull.getErrorCode());

		// blank
		MemberException exBlank = assertThrows(MemberException.class, () -> memberService.isNicknameUnique("   "));
		assertEquals(ErrorCode.NICKNAME_INVALID, exBlank.getErrorCode());

		// repository는 호출되지 않아야 함
		verify(memberRepository, never()).findByNickname(anyString());
	}

	@Test
	@DisplayName("findActiveByNicknameIgnoreCase: null 또는 공백 입력이면 리포지토리 호출 없이 Optional.empty 반환")
	void findActiveByNicknameIgnoreCase_NullOrBlank_ReturnsEmpty() {
		// null
		Optional<Member> resNull = memberService.findActiveByNicknameIgnoreCase(null);
		assertTrue(resNull.isEmpty());
		verify(memberRepository, never()).findActiveByNicknameIgnoreCase(anyString());

		// blank
		Optional<Member> resBlank = memberService.findActiveByNicknameIgnoreCase("   ");
		assertTrue(resBlank.isEmpty());
		verify(memberRepository, never()).findActiveByNicknameIgnoreCase(anyString());
	}

	@Test
	@DisplayName("findActiveByNicknameIgnoreCase: 리포지토리 결과를 그대로 반환하고 키는 normalize 되어 호출된다")
	void findActiveByNicknameIgnoreCase_WhenFound_ReturnsMember() {
		// given
		String raw = "TestNick";
		String normalized = MemberService.normalize(raw); // "testnick"
		Member member = new Member(profile, additionalInfo, fixedClock);

		when(memberRepository.findActiveByNicknameIgnoreCase(normalized)).thenReturn(Optional.of(member));

		// when
		Optional<Member> result = memberService.findActiveByNicknameIgnoreCase(raw);

		// then
		assertTrue(result.isPresent());
		assertEquals(member, result.get());
		verify(memberRepository, times(1)).findActiveByNicknameIgnoreCase(normalized);
	}

	// ----- 추가된 테스트들 (getActiveByNicknameOrThrow 성공 / 실패) -----

	@Test
	@DisplayName("getActiveByNicknameOrThrow: 존재하면 Member 반환")
	void getActiveByNicknameOrThrow_WhenFound_ReturnsMember() {
		// given
		String raw = "FoundNick";
		String normalized = MemberService.normalize(raw);
		Member member = new Member(profile, additionalInfo, fixedClock);

		when(memberRepository.findActiveByNicknameIgnoreCase(normalized)).thenReturn(Optional.of(member));

		// when
		Member result = memberService.getActiveByNicknameOrThrow(raw);

		// then
		assertNotNull(result);
		assertEquals(member, result);
		verify(memberRepository, times(1)).findActiveByNicknameIgnoreCase(normalized);
	}

	@Test
	@DisplayName("getActiveByNicknameOrThrow: 존재하지 않으면 MemberException(MEMBER_NOT_FOUND) 발생")
	void getActiveByNicknameOrThrow_WhenNotFound_ThrowsMemberException() {
		// given
		String raw = "NoSuchNick";
		String normalized = MemberService.normalize(raw);

		when(memberRepository.findActiveByNicknameIgnoreCase(normalized)).thenReturn(Optional.empty());

		// when / then
		MemberException ex = assertThrows(MemberException.class, () -> memberService.getActiveByNicknameOrThrow(raw));
		assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
		verify(memberRepository, times(1)).findActiveByNicknameIgnoreCase(normalized);
	}
}