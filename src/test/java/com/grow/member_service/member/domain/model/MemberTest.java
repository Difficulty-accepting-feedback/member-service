package com.grow.member_service.member.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.member.domain.exception.MemberDomainException;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.service.MemberService;

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
	@DisplayName("Clock 없이 생성 시 createAt이 현재 시각 범위 내에 설정된다")
	void constructor_WithoutClock_SetsCreateAt() {
		LocalDateTime before = LocalDateTime.now();
		Member member = new Member(profile, additionalInfo, null);
		LocalDateTime after = LocalDateTime.now();

		assertFalse(member.getCreateAt().isBefore(before),
			"createAt should not be before instantiation");
		assertFalse(member.getCreateAt().isAfter(after),
			"createAt should not be after instantiation");
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
	void addPoint_Negative_ThrowsDomainException() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		MemberDomainException ex = assertThrows(
			MemberDomainException.class,
			() -> member.addPoint(-1)
		);
		assertTrue(ex.getMessage().contains("0 이상"),
			"Exception message should mention non-negative requirement");
	}

	@Test
	@DisplayName("adjustScore(): score가 올바르게 증가 및 감소한다")
	void adjustScore_ChangesScore() {
		Member member = new Member(profile, additionalInfo, fixedClock);
		assertEquals(36.5, member.getScore());

		member.adjustScore(2.5);
		assertEquals(39.0, member.getScore());

		member.adjustScore(-1.5);
		assertEquals(37.5, member.getScore());
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

	@Test
	@DisplayName("verifyPhone(): null phoneNumber 전달 시 MemberDomainException 발생")
	void verifyPhone_NullPhone_ThrowsInvalidPhoneNumber() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		assertThrows(
			MemberDomainException.class,
			() -> member.verifyPhone(null)
		);
	}

	@Test
	@DisplayName("verifyPhone(): blank phoneNumber 전달 시 MemberDomainException 발생")
	void verifyPhone_BlankPhone_ThrowsInvalidPhoneNumber() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		assertThrows(
			MemberDomainException.class,
			() -> member.verifyPhone("   ")
		);
	}

	@Test
	@DisplayName("verifyPhone(): 이미 인증된 경우 예외 발생")
	void verifyPhone_AlreadyVerified_ThrowsAlreadyPhoneVerified() {
		Member member = new Member(profile, additionalInfo, fixedClock);
		member.verifyPhone("01012345678");

		MemberDomainException ex = assertThrows(
			MemberDomainException.class,
			() -> member.verifyPhone("01012345678")
		);
		assertTrue(ex.getMessage().contains("이미") && ex.getMessage().contains("인증"),
			"Exception message should mention already verified");
	}

	@Test
	@DisplayName("changeNickname(): 고유한 닉네임 입력 시 nickname이 변경된다")
	void changeNickname_Unique_SetsNewNickname() {
		Member member = new Member(profile, additionalInfo, fixedClock);
		MemberService service = nickname -> true;

		member.changeNickname("newNick", service);

		assertEquals("newNick", member.getMemberProfile().getNickname());
	}

	@Test
	@DisplayName("changeNickname(): null nickname 전달 시 NullPointerException 발생")
	void changeNickname_NullNickname_ThrowsNullPointerException() {
		Member member = new Member(profile, additionalInfo, fixedClock);
		MemberService service = nickname -> true;

		NullPointerException ex = assertThrows(
			NullPointerException.class,
			() -> member.changeNickname(null, service)
		);
		assertEquals("변경할 닉네임은 null일 수 없습니다.", ex.getMessage());
	}

	@Test
	@DisplayName("changeNickname(): 중복된 닉네임 입력 시 예외 발생")
	void changeNickname_Duplicate_ThrowsDomainException() {
		Member member = new Member(profile, additionalInfo, fixedClock);
		MemberService service = nickname -> false;

		MemberDomainException ex = assertThrows(
			MemberDomainException.class,
			() -> member.changeNickname("dupNick", service)
		);
		assertTrue(ex.getMessage().contains("dupNick"),
			"Exception message should include the duplicate nickname");
	}

	@Test
	@DisplayName("changeProfileImage(): 새로운 프로필 이미지 입력 시 변경된다")
	void changeProfileImage_SetsNewProfileImage() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		member.changeProfileImage("http://example.com/new.png");

		assertEquals("http://example.com/new.png",
			member.getMemberProfile().getProfileImage());
	}

	@Test
	@DisplayName("changeAddress(): 새로운 주소 입력 시 변경된다")
	void changeAddress_SetsNewAddress() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		member.changeAddress("Busan");

		assertEquals("Busan", member.getAdditionalInfo().getAddress());
	}

	@Test
	@DisplayName("changeAddress(): null address 전달 시 NullPointerException 발생")
	void changeAddress_NullAddress_ThrowsNullPointerException() {
		Member member = new Member(profile, additionalInfo, fixedClock);

		NullPointerException ex = assertThrows(
			NullPointerException.class,
			() -> member.changeAddress(null)
		);
		assertEquals("변경할 주소는 null일 수 없습니다.", ex.getMessage());
	}
}