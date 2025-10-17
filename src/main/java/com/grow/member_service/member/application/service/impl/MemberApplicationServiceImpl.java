package com.grow.member_service.member.application.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.achievement.challenge.domain.model.enums.ChallengeIds;
import com.grow.member_service.achievement.trigger.event.AchievementTriggerEvent;
import com.grow.member_service.achievement.trigger.listener.AchievementTriggerProducer;
import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.event.NotificationType;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.util.JsonUtils;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.application.dto.MemberPublicResponse;
import com.grow.member_service.member.application.dto.ResolveMemberResponse;
import com.grow.member_service.member.application.event.MemberNotificationEvent;
import com.grow.member_service.member.application.port.GeoIndexPort;
import com.grow.member_service.member.application.service.MemberApplicationService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.MemberWithdrawalLogRepository;
import com.grow.member_service.member.domain.service.MemberService;
import com.grow.member_service.member.presentation.dto.MemberUpdateRequest;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberApplicationServiceImpl implements MemberApplicationService {

	private static final java.time.Duration DEDUPE_TTL = java.time.Duration.ofHours(12);
	private static final String TOPIC = "member.notification.requested";
	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final MemberWithdrawalLogRepository withdrawalLogRepository;
	private final ObjectProvider<GeoIndexPort> geoIndexProvider;
	private final AchievementTriggerProducer achievementTriggerProducer;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectProvider<StringRedisTemplate> redisProvider;

	@Transactional(readOnly = true)
	@Override
	public MemberInfoResponse getMyInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		// 온보딩 리마인더 알림 발송 (중복 방지 포함)
		publishOnboardingReminderEvent(member);

		log.info("[MEMBER] 내 정보 조회 요청 - memberId={}", memberId);

		return MemberInfoResponse.from(member);
	}

	@Override
	@Transactional
	@Timed(value="member_withdraw_latency")
	@Counted(value="member_withdraw_total")
	public void withdraw(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		UUID uuid = UUID.randomUUID();
		LocalDateTime now = LocalDateTime.now();

		// 로그 저장
		withdrawalLogRepository.saveFromMember(member);

		// 마스킹 및 탈퇴 처리
		member.markAsWithdrawn(uuid);
		memberRepository.save(member);

		GeoIndexPort geoIndexPort = geoIndexProvider.getIfAvailable();
		if (geoIndexPort != null) {
			geoIndexPort.remove(memberId);
		}

		log.info("[MEMBER] 탈퇴 처리 완료 - memberId={}, withdrawnAt={}", memberId, now);
	}

	@Override
	@Transactional
	public void updateMember(Long memberId, MemberUpdateRequest req) {
		log.info("[MEMBER] 회원 정보 업데이트 요청 - memberId={}", memberId);
		log.debug("[MEMBER] 요청 본문 - nickname='{}', profileImage='{}', address='{}'",
			req.getNickname(), req.getProfileImage(), req.getAddress());

		Member m = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		boolean nicknameChanged = false;
		boolean addressChanged = false;
		boolean imageChanged = false;

		// 실제 값이 바뀐 경우에만 변경 (자기 자신 중복 예외 방지)
		if (req.getNickname() != null) {
			String next = req.getNickname().trim();
			String current = m.getMemberProfile().getNickname();
			if (!next.equals(current)) {
				log.info("[MEMBER] 닉네임 변경 - '{}' -> '{}'", current, next);
				m.changeNickname(next, memberService);
				nicknameChanged = true;
			} else {
				log.debug("[MEMBER] 닉네임 변경 스킵 - 동일 값('{}')", current);
			}
		}

		// 프로필 이미지는 null 포함 항상 덮어쓰기
		if (!Objects.equals(req.getProfileImage(), m.getMemberProfile().getProfileImage())) {
			log.info("[MEMBER] 프로필 이미지 변경 - '{}' -> '{}'",
				m.getMemberProfile().getProfileImage(), req.getProfileImage());
			m.changeProfileImage(req.getProfileImage());
			imageChanged = true;
		} else {
			log.debug("[MEMBER] 프로필 이미지 변경 스킵 - 동일 값");
		}

		// 주소-> 값이 들어왔고 실제로 바뀐 경우에만 변경 (구 단위 문자열)
		if (req.getAddress() != null) {
			String nextAddr = req.getAddress().trim();
			String currAddr = m.getAdditionalInfo().getAddress();
			if (!Objects.equals(nextAddr, currAddr)) {
				log.info("[MEMBER] 주소 변경 - '{}' -> '{}'", currAddr, nextAddr);
				m.changeAddress(nextAddr);
				addressChanged = true;
			} else {
				log.debug("[MEMBER] 주소 변경 스킵 - 동일 값");
			}
		}

		memberRepository.save(m);

		// 업적 이벤트
		if (addressChanged) {
			try {
				achievementTriggerProducer.send(
					new AchievementTriggerEvent(
						memberId,
						ChallengeIds.ADDRESS_SET,
						"ADDRESS_SET",
						"ADDR-" + memberId,
						LocalDateTime.now()
					)
				);
				log.info("[MEMBER][ACHIEVEMENT] ADDRESS_SET 트리거 전송 완료 - memberId={}", memberId);
			} catch (Exception ex) {
				log.warn("[MEMBER][ACHIEVEMENT] ADDRESS_SET 트리거 전송 실패 - memberId={}, err={}",
					memberId, ex.toString(), ex);
			}
		}

		log.info("[MEMBER] 회원 정보 업데이트 완료 - memberId={}, changed[nickname={}, image={}, address={}]",
			memberId, nicknameChanged, imageChanged, addressChanged);
	}

	private Member findById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
	}

	@Transactional
	@Override
	public void toggleMatching(Long memberId, boolean enabled) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		if (member.isMatchingEnabled() == enabled) {
			log.debug("[MEMBER] 매칭 기능 변경 스킵 - 이미 동일 상태, memberId={}, enabled={}", memberId, enabled);
			return;
		}

		if (enabled) {
			member.enableMatching();
		} else {
			member.disableMatching();
		}

		memberRepository.save(member);
		log.info("[MEMBER] 매칭 기능 상태 변경 - memberId={}, enabled={}", memberId, enabled);
	}

	/**
	 * 닉네임으로 회원 조회
	 */
	@Transactional(readOnly = true)
	@Override
	public ResolveMemberResponse resolveByNickname(String nickname) {
		Member m = memberService.getActiveByNicknameOrThrow(nickname);
		return new ResolveMemberResponse(m.getMemberId(), m.getMemberProfile().getNickname());
	}

	/**
	 * 공개용 회원 정보 조회
	 */
	@Transactional(readOnly = true)
	@Override
	public MemberPublicResponse getByIdPublic(Long memberId) {
		Member m = findById(memberId);
		return MemberPublicResponse.of(m);
	}

	private void publishOnboardingReminderEvent(Member m) {
		if (m.needsAddressReminder()) {
			sendIfDedupeOk(
				buildDedupeKey(NotificationType.ADDR_REMINDER.name(), m.getMemberId()),
				createMemberNotificationEvent(m.getMemberId(), NotificationType.ADDR_REMINDER)
			);
		}
		if (!m.isPhoneVerified()) {
			sendIfDedupeOk(
				buildDedupeKey(NotificationType.PHONE_REMINDER.name(), m.getMemberId()),
				createMemberNotificationEvent(m.getMemberId(), NotificationType.PHONE_REMINDER)
			);
		}
	}

	/**
	 * MemberNotificationEvent 생성
	 * @param memberId 회원 ID
	 * @param type 알림 타입
	 * @return 생성된 이벤트 객체
	 */
	private MemberNotificationEvent createMemberNotificationEvent(Long memberId, NotificationType type) {
		return new MemberNotificationEvent(
			memberId,
			type.name(),
			type.getNotificationType(),
			type.getTitle(),
			type.getContent(),
			LocalDateTime.now()
		);
	}

	/**
	 * 중복 방지 후 Kafka 전송
	 * 중복 방지 키가 이미 존재하면 스킵
	 * 중복 방지 키는 12시간 TTL로 설정
	 * 중복 방지 키 형식: notify:onboard:{code}:{memberId}
	 * ex) notify:onboard:ADDR_REMINDER:123
	 * @param dedupeKey 중복 방지 키
	 * @param event 전송할 이벤트
	 */
	private void sendIfDedupeOk(String dedupeKey, MemberNotificationEvent event) {
		if (!acquireOnce(dedupeKey, DEDUPE_TTL)) {
			log.debug("[MEMBER][NOTIFY] 알림 전송 스킵 - dedupe hit, key={}", dedupeKey);
			return;
		}
		String key = event.memberId().toString();
		String payload = JsonUtils.toJsonString(event);
		kafkaTemplate.send(TOPIC, key, payload);
		log.info("[MEMBER][KAFKA][NOTIFY] Kafka 전송 완료 - code={}", event.code());
	}

	/**
	 * Redis SETNX로 12시간 동안 1회만 수행
	 * 로컬/테스트 환경에서는 항상 true 반환
	 * 중복 방지 키가 이미 존재하면 false 반환
	 * 중복 방지 키가 없으면 생성 후 true 반환
	 * 중복 방지 키는 ttl 동안 유지
	 * 중복 방지 키 형식: notify:onboard:{code}:{memberId}
	 * @param key 중복 방지 키
	 * @param ttl 유지 시간
	 * @return 처리 가능 여부
	 */
	private boolean acquireOnce(String key, java.time.Duration ttl) {
		StringRedisTemplate r = redisProvider.getIfAvailable();
		if (r == null)
			return true;
		Boolean ok = r.opsForValue().setIfAbsent(key, "1", ttl);
		return Boolean.TRUE.equals(ok);
	}

	/**
	 * 중복 방지 키 생성
	 * @param code 알림 코드
	 * @param memberId 회원 ID
	 * @return 중복 방지 키
	 */
	private String buildDedupeKey(String code, Long memberId) {
		return "notify:onboard:" + code + ":" + memberId;
	}
}