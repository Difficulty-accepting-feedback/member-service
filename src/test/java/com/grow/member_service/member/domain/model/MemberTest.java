package com.grow.member_service.member.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.member.domain.exception.MemberDomainException;

class MemberTest {

	private Clock fixedClock;
	private LocalDateTime fixedNow;
	private MemberProfile profile;
	private MemberAdditionalInfo additionalInfo;

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
	@DisplayName("Clock 주입 시 createAt이 고정된 시간으로 설정, 기본 값 초기화")
	void constructor_WithClock_SetsCreateAtAndDefaults() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		assertEquals(fixedNow, member.getCreateAt());
		assertEquals(profile, member.getMemberProfile());
		assertEquals(additionalInfo, member.getAdditionalInfo());
		assertEquals(0, member.getTotalPoint());
		assertEquals(36.5, member.getScore());
		assertFalse(member.isWithdrawn());
		assertFalse(member.isPhoneVerified());
	}

	@Test
	@DisplayName("markAsWithdrawn(): 처음 호출 시 withdrawalAt이 설정되고 isWithdrawn()이 true가 됨")
	void markAsWithdrawn_FirstTime_SetsWithdrawalAtAndIsWithdrawn() {
		Member member = new Member(profile, additionalInfo, fixedClock);
		assertFalse(member.isWithdrawn());

		UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
		member.markAsWithdrawn(fixedUuid);

		assertNotNull(member.getWithdrawalAt());
		assertTrue(member.isWithdrawn());
	}

	@Test
	@DisplayName("markAsWithdrawn(): 이미 탈퇴된 회원에 대해 두 번째 호출 시 예외 발생")
	void markAsWithdrawn_SecondTime_ThrowsAlreadyWithdrawn() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
		member.markAsWithdrawn(fixedUuid);

		MemberDomainException ex = assertThrows(
			MemberDomainException.class,
			() -> member.markAsWithdrawn(fixedUuid)
		);
		assertEquals("이미 탈퇴한 회원입니다.", ex.getMessage());
	}

	@Test
	@DisplayName("addPoint(): 양수 포인트 추가 시 totalPoint가 증가한다")
	void addPoint_Positive_IncreasesTotalPoint() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		member.addPoint(10);
		assertEquals(10, member.getTotalPoint());

		member.addPoint(5);
		assertEquals(15, member.getTotalPoint());
	}

	@Test
	@DisplayName("addPoint(): 음수 포인트 추가 시 예외 발생")
	void addPoint_Negative_ThrowsIllegalArgumentException() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		MemberDomainException ex = assertThrows(
			MemberDomainException.class,
			() -> member.addPoint(-1)
		);
		assertEquals("부여할 포인트는 0 이상이어야 합니다. 입력값: -1", ex.getMessage());
	}

	@Test
	@DisplayName("verifyPhone(): 핸드폰 인증 처리 후 isPhoneVerified()가 true가 된다")
	void verifyPhone_SetsAdditionalInfoPhoneVerified() {
		Member member = new Member(profile, additionalInfo, fixedClock);
		assertFalse(member.isPhoneVerified());

		member.verifyPhone("01012345678");

		assertTrue(member.isPhoneVerified());
		assertEquals("01012345678", member.getAdditionalInfo().getPhoneNumber());
	}
}