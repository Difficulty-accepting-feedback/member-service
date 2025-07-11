package com.grow.member_service.member.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PhoneVerificationTest {

	@Test
	@DisplayName("newRequest(): id는 null, memberId·phoneNumber는 전달값, code는 6자리 숫자, verified는 false, createdAt은 설정된다")
	void newRequest_SetsFieldsCorrectly() {
		Long memberId = 42L;
		String phone = "01012345678";

		PhoneVerification request = PhoneVerification.newRequest(memberId, phone);

		assertNull(request.getId(),    "newRequest 시 id는 null이어야 한다");
		assertEquals(memberId, request.getMemberId());
		assertEquals(phone,    request.getPhoneNumber());
		assertNotNull(request.getCode(), "인증 코드는 null이 아니어야 한다");
		assertEquals(6,        request.getCode().length(), "코드는 6자리여야 한다");
		assertTrue(request.getCode().matches("\\d{6}"), "코드는 모두 숫자여야 한다");
			assertFalse(request.isVerified(),  "초기 verified는 false여야 한다");
		assertNotNull(request.getCreatedAt(), "createdAt은 설정되어야 한다");
	}

	@Test
	@DisplayName("verify(): 올바른 코드 입력 시 verified=true 상태의 새 인스턴스를 반환한다")
	void verify_CorrectCode_ReturnsVerifiedInstance() {
		Instant now = Instant.now();
		PhoneVerification original = new PhoneVerification(
			10L, 5L, "01000000000", "123456", now, false
		);

		PhoneVerification verified = original.verify("123456");

		assertNotSame(original, verified, "verify는 새로운 인스턴스를 반환해야 한다");
		assertEquals(original.getId(),        verified.getId());
		assertEquals(original.getMemberId(),  verified.getMemberId());
		assertEquals(original.getPhoneNumber(), verified.getPhoneNumber());
		assertEquals(original.getCode(),      verified.getCode());
		assertEquals(original.getCreatedAt(), verified.getCreatedAt());
		assertTrue(verified.isVerified(), "verify 후 verified는 true여야 한다");
	}

	@Test
	@DisplayName("verify(): 잘못된 코드 입력 시 IllegalArgumentException 발생")
	void verify_IncorrectCode_ThrowsException() {
		PhoneVerification original = new PhoneVerification(
			1L, 2L, "01000000000", "654321", Instant.now(), false
		);

		IllegalArgumentException ex = assertThrows(
			IllegalArgumentException.class,
			() -> original.verify("000000")
		);
		assertEquals("인증 코드가 일치하지 않습니다.", ex.getMessage());
	}
}