package com.grow.member_service.achievement.trigger.event;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.achievement.challenge.domain.model.enums.ChallengeIds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 업적 달성 이벤트 발행기
 * - 최초 달성시에만 이벤트 발행
 * - 중복 달성 시도시 무시
 * - 편의 메서드 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementTriggerPublisher {

	private final ApplicationEventPublisher publisher;
	private final AccomplishedRepository accomplishedRepository;

	/**
	 * 최초 달성시에만 이벤트 발행
	 * @param memberId memberId
	 * @param challengeId challengeId
	 * @return true=이벤트 발행, false=이미 달성함(이벤트 미발행)
	 */
	public boolean publishIfFirst(Long memberId, long challengeId) {
		boolean already = accomplishedRepository
			.existsByMemberIdAndChallengeId(memberId, challengeId);
		if (already) {
			log.debug("[업적] 달성한 업적, skip publish: memberId={}, challengeId={}",
				memberId, challengeId);
			return false;
		}
		publisher.publishEvent(new AchievementTriggerEvent(
			memberId, challengeId, LocalDateTime.now()));
		log.info("[업적] 업적 달성 -> published: memberId={}, challengeId={}",
			memberId, challengeId);
		return true;
	}

	// 편의 메서드
	public boolean publishAddressSetIfFirst(Long memberId) {
		return publishIfFirst(memberId, ChallengeIds.ADDRESS_SET);
	}
	public boolean publishPhoneVerifiedIfFirst(Long memberId) {
		return publishIfFirst(memberId, ChallengeIds.PHONE_VERIFIED);
	}
	public boolean publishFirstLoginIfFirst(Long memberId) {
		return publishIfFirst(memberId, ChallengeIds.FIRST_LOGIN);
	}
	public boolean publishFirstPaymentIfFirst(Long memberId) {
		return publishIfFirst(memberId, ChallengeIds.FIRST_PAYMENT);
	}
	public boolean publishFirstSubscriptionIfFirst(Long memberId) {
		return publishIfFirst(memberId, ChallengeIds.FIRST_SUBSCRIPTION);
	}
}