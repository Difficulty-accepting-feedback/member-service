package com.grow.member_service.member.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.PhoneVerification;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.PhoneVerificationRepository;
import com.grow.member_service.member.domain.service.SmsService;

class PhoneVerificationServiceTest {

	@Mock
	private PhoneVerificationRepository verificationRepo;
	@Mock
	private SmsService smsService;
	@Mock
	private MemberRepository memberRepo;

	@InjectMocks
	private PhoneVerificationService service;

	private final Long MEMBER_ID = 42L;
	private final String PHONE   = "01012345678";
	private final String CODE    = "999999";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void requestVerification_shouldSaveAndSendSms() {
		// given
		PhoneVerification toSave = PhoneVerification.newRequest(MEMBER_ID, PHONE);
		PhoneVerification saved   = new PhoneVerification(
			1L, MEMBER_ID, PHONE, CODE, Instant.now(), false
		);
		when(verificationRepo.save(any())).thenReturn(saved);

		// when
		Long id = service.requestVerification(MEMBER_ID, PHONE);

		// then
		assertEquals(1L, id);
		// repository.save 에는 도메인 객체가 들어가야 한다
		ArgumentCaptor<PhoneVerification> captor = ArgumentCaptor.forClass(PhoneVerification.class);
		verify(verificationRepo).save(captor.capture());
		PhoneVerification arg = captor.getValue();
		assertNull(arg.getId());
		assertEquals(MEMBER_ID, arg.getMemberId());
		assertEquals(PHONE, arg.getPhoneNumber());
		assertFalse(arg.isVerified());

		// smsService.send 호출 검증
		verify(smsService).send(eq(PHONE), contains("인증 코드: "));
	}

	@Test
	void verifyCode_withCorrectCode_shouldMarkMemberVerified() {
		// given: 기존 PhoneVerification
		PhoneVerification existing = new PhoneVerification(
			10L, MEMBER_ID, PHONE, CODE, Instant.now(), false
		);
		when(verificationRepo.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(existing));
		// v.verify() 리턴 객체
		PhoneVerification verified = new PhoneVerification(
			10L, MEMBER_ID, PHONE, CODE, Instant.now(), true
		);
		// 실제 v.verify() 를 쓰기 때문에 repository.save(verified) 호출 기대
		when(verificationRepo.save(any())).thenReturn(verified);

		// given: 기존 Member
		MemberProfile profile = new MemberProfile("a@b.com","nick",null,Platform.KAKAO,"pid");
		MemberAdditionalInfo addInfo = new MemberAdditionalInfo(null,null);
		Clock fixedClock = Clock.fixed(Instant.parse("2025-07-10T00:00:00Z"), ZoneId.of("UTC"));
		Member member = new Member(profile, addInfo, fixedClock);
		when(memberRepo.findById(MEMBER_ID)).thenReturn(Optional.of(member));

		// when
		service.verifyCode(MEMBER_ID, CODE);

		// then
		// PhoneVerificationRepository.save 호출
		verify(verificationRepo).save(any(PhoneVerification.class));
		// MemberRepository.findById + save 호출
		verify(memberRepo).findById(MEMBER_ID);
		verify(memberRepo).save(member);
		// member.phoneVerified 가 true 로 변경됐는지
		assertTrue(member.isPhoneVerified());
		assertEquals(PHONE, member.getAdditionalInfo().getPhoneNumber());
	}

	@Test
	void verifyCode_withWrongCode_shouldThrow() {
		// given
		PhoneVerification existing = new PhoneVerification(
			10L, MEMBER_ID, PHONE, "123456", Instant.now(), false
		);
		when(verificationRepo.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(existing));

		// when / then
		assertThrows(IllegalArgumentException.class,
			() -> service.verifyCode(MEMBER_ID, CODE)
		);
		// save 는 호출되지 않아야 함
		verify(verificationRepo, never()).save(any());
		verify(memberRepo, never()).save(any());
	}
}