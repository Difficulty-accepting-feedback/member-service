package com.grow.member_service.member.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberAdditionalInfoTest {

	@Test
	@DisplayName("전달된 전화번호·주소가 설정되고, phoneVerified는 false")
	void constructor_Default_SetsFieldsAndUnverified() {
		MemberAdditionalInfo info = new MemberAdditionalInfo("01012345678", "Seoul");

		assertEquals("01012345678", info.getPhoneNumber());
		assertEquals("Seoul", info.getAddress());
		assertFalse(info.isPhoneVerified(), "기본 생성 시 phoneVerified는 false여야 한다");
	}

	@Test
	@DisplayName("phoneVerified 값을 true로 설정할 수 있다")
	void constructor_WithVerifiedFlag_SetsVerified() {
		MemberAdditionalInfo info = new MemberAdditionalInfo("01012345678", "Seoul", true);

		assertEquals("01012345678", info.getPhoneNumber());
		assertEquals("Seoul", info.getAddress());
		assertTrue(info.isPhoneVerified(), "생성자 인자로 true를 주면 phoneVerified가 true여야 한다");
	}

	@Test
	@DisplayName("verifyPhone 호출 시 새로운 인스턴스가 반환되고, phoneVerified가 true로 전환")
	void verifyPhone_ReturnsNewInstanceWithVerifiedTrue() {
		MemberAdditionalInfo original = new MemberAdditionalInfo("01012345678", "Seoul");
		MemberAdditionalInfo verified = original.verifyPhone("01012345678");

		assertNotSame(original, verified, "verifyPhone는 새로운 인스턴스를 반환");
		assertEquals("01012345678", verified.getPhoneNumber());
		assertEquals("Seoul", verified.getAddress());
		assertTrue(verified.isPhoneVerified(), "verifyPhone 이후 phoneVerified는 true여야 한다");
	}

	@Test
	@DisplayName("null 또는 빈 문자열 입력 시 phoneNumber는 null로 저장")
	void validatePhoneNumber_NullOrEmpty_SetsNull() {
		MemberAdditionalInfo infoNull = new MemberAdditionalInfo(null, "Addr");
		assertNull(infoNull.getPhoneNumber(), "null 입력 시 phoneNumber는 null이어야 한다");

		MemberAdditionalInfo infoEmpty = new MemberAdditionalInfo("   ", "Addr");
		assertNull(infoEmpty.getPhoneNumber(), "빈 문자열 입력 시 phoneNumber는 null이어야 한다");
	}
}