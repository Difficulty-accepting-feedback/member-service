package com.grow.member_service.member.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.event.MemberNotificationPublisher;
import com.grow.member_service.member.application.service.impl.PhoneVerificationServiceImpl;
import com.grow.member_service.member.domain.exception.MemberDomainException;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.PhoneVerification;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.PhoneVerificationRepository;
import com.grow.member_service.member.domain.service.SmsService;

;

@DisplayName("PhoneVerificationService 테스트")
class PhoneVerificationServiceImplTest {

	@Mock private PhoneVerificationRepository verificationRepo;

	@Mock private SmsService smsService;

	@Mock private MemberRepository memberRepo;

	@Mock private MemberNotificationPublisher notificationPublisher;

	@InjectMocks
	private PhoneVerificationServiceImpl service;

	private final Long MEMBER_ID = 42L;
	private final String PHONE   = "01012345678";
	private final String CODE    = "999999";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("requestVerification(): 새로운 인증 요청 저장 후 SMS 전송 및 ID 반환")
	void requestVerification_shouldSaveAndSendSms() {
		// given
		PhoneVerification saved = new PhoneVerification(
			1L, MEMBER_ID, PHONE, CODE, Instant.now(), false
		);
		when(verificationRepo.save(any())).thenReturn(saved);

		// when
		Long id = service.requestVerification(MEMBER_ID, PHONE);

		// then
		assertEquals(1L, id);
		ArgumentCaptor<PhoneVerification> captor = ArgumentCaptor.forClass(PhoneVerification.class);
		verify(verificationRepo).save(captor.capture());
		PhoneVerification arg = captor.getValue();
		assertNull(arg.getId());
		assertEquals(MEMBER_ID, arg.getMemberId());
		assertEquals(PHONE, arg.getPhoneNumber());
		assertFalse(arg.isVerified());
		verify(smsService).send(eq(PHONE), contains("인증 코드: "));
	}

	@Test
	@DisplayName("requestVerification(): SMS 전송 실패 시 예외 발생")
	void requestVerification_whenSmsSendFails_shouldThrowMemberDomainException() {
		// given
		PhoneVerification toSave = PhoneVerification.newRequest(MEMBER_ID, PHONE);
		when(verificationRepo.save(any())).thenReturn(toSave);
		doThrow(new MemberException(ErrorCode.SMS_SEND_FAILED)).when(smsService).send(any(), any());

		// when / then
		assertThrows(MemberException.class,
			() -> service.requestVerification(MEMBER_ID, PHONE)
		);
		verify(verificationRepo).save(any());
		verify(smsService).send(any(), any());
	}

	@Test
	@DisplayName("verifyCode(): 올바른 코드 입력 시 회원 핸드폰 인증 처리 및 저장")
	void verifyCode_withCorrectCode_shouldMarkMemberVerified() {
		// given
		PhoneVerification existing = new PhoneVerification(
			10L, MEMBER_ID, PHONE, CODE, Instant.now(), false
		);
		when(verificationRepo.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(existing));
		when(verificationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

		MemberProfile profile = new MemberProfile(
			"a@b.com", "nick", null, Platform.KAKAO, "pid"
		);
		MemberAdditionalInfo addInfo = new MemberAdditionalInfo(null, null);
		Member member = new Member(profile, addInfo,
			Clock.fixed(Instant.parse("2025-07-10T00:00:00Z"), ZoneId.of("UTC")));
		when(memberRepo.findById(MEMBER_ID)).thenReturn(Optional.of(member));
		when(memberRepo.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

		// when
		service.verifyCode(MEMBER_ID, CODE);

		// then
		verify(verificationRepo).save(any(PhoneVerification.class));
		verify(memberRepo).save(member);
		assertTrue(member.isPhoneVerified());
		assertEquals(PHONE, member.getAdditionalInfo().getPhoneNumber());
	}

	@Test
	@DisplayName("verifyCode(): 잘못된 코드 입력 시 예외 발생")
	void verifyCode_withWrongCode_shouldThrow() {
		// given
		PhoneVerification existing = new PhoneVerification(
			10L, MEMBER_ID, PHONE, "123456", Instant.now(), false
		);
		when(verificationRepo.findByMemberId(MEMBER_ID))
			.thenReturn(Optional.of(existing));

		// when / then
		assertThrows(MemberDomainException.class,
			() -> service.verifyCode(MEMBER_ID, CODE)
		);

		// save는 호출되지 않아야 함
		verify(verificationRepo, never()).save(any());
		verify(memberRepo, never()).save(any());
	}
}