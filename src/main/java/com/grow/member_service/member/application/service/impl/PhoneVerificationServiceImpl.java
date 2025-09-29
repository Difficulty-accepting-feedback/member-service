package com.grow.member_service.member.application.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.achievement.challenge.domain.model.enums.ChallengeIds;
import com.grow.member_service.achievement.trigger.event.AchievementTriggerEvent;
import com.grow.member_service.achievement.trigger.listener.AchievementTriggerProducer;
import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.event.MemberNotificationPublisher;
import com.grow.member_service.member.application.service.PhoneVerificationService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.PhoneVerification;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.PhoneVerificationRepository;
import com.grow.member_service.member.domain.service.SmsService;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PhoneVerificationServiceImpl implements PhoneVerificationService {

	private final PhoneVerificationRepository repository;
	private final SmsService smsService;
	private final MemberRepository memberRepository;
	private final MemberNotificationPublisher notificationPublisher;
	private final AchievementTriggerProducer achievementTriggerProducer;
	private final MeterRegistry meterRegistry;

	/**
	 * 소셜 가입 직후 호출되어,
	 * 도메인 모델로부터 인증 요청 객체 생성,
	 * 저장 후 반환된 엔티티에서 코드 획득
	 * -> SmsService 포트를 통해 SMS 전송
	 *
	 * @return 생성된 PhoneVerification 의 ID
	 */
	@Override
	@Timed(value="phone_verify_send_latency")
	@Counted(value="phone_verify_send_total")
	public Long requestVerification(Long memberId, String phoneNumber) {
		PhoneVerification verification = PhoneVerification.newRequest(memberId, phoneNumber);
		PhoneVerification saved = repository.save(verification);
		smsService.send(saved.getPhoneNumber(), "인증 코드: " + saved.getCode());
		meterRegistry.counter("phone_verify_send_successes").increment();
		return saved.getId();
	}

	/**
	 * 사용자 입력 코드를 검증하고,
	 * 성공 시 PhoneVerification 저장 후
	 * Member 도메인에 인증 완료 처리
	 */
	@Override
	@Timed(value="phone_verify_check_latency")
	@Counted(value="phone_verify_check_total")
	public void verifyCode(Long memberId, String code) {
		// 1) PhoneVerification 검증
		PhoneVerification v = repository.findByMemberId(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.PHONE_VERIFICATION_NOT_REQUESTED));
		PhoneVerification verified = v.verify(code);
		repository.save(verified);

		// 2) Member 도메인에 인증 정보 반영
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
		member.verifyPhone(verified.getPhoneNumber());
		memberRepository.save(member);

		// 3) 인증 성공 알림
		notificationPublisher.publishPhoneVerifiedSuccess(memberId);

		meterRegistry.counter("phone_verify_check_successes").increment();

		// 업적 이벤트
		try {
			achievementTriggerProducer.send(
				new AchievementTriggerEvent(
					memberId,
					ChallengeIds.PHONE_VERIFIED,
					"PHONE_VERIFIED",
					"PHONE-" + memberId,
					LocalDateTime.now()
				)
			);
			log.info("[ACHV][TRIGGER] PHONE_VERIFIED sent - memberId={}", memberId);
		} catch (Exception ex) {
			log.warn("[ACHV][TRIGGER][SEND-FAIL] PHONE_VERIFIED - memberId={}, err={}",
				memberId, ex.toString(), ex);
		}
	}

	/**
	 * 인증 여부 조회
	 */
	@Override
	public boolean isPhoneVerified(Long memberId) {
		return memberRepository.findById(memberId)
			.map(Member::isPhoneVerified)
			.orElse(false);
	}
}