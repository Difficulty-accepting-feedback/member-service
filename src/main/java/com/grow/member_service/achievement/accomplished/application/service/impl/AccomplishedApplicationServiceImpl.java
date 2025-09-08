package com.grow.member_service.achievement.accomplished.application.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.achievement.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.achievement.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.achievement.accomplished.application.model.AccomplishedPeriod;
import com.grow.member_service.achievement.accomplished.application.service.AccomplishedApplicationService;
import com.grow.member_service.achievement.accomplished.domain.model.Accomplished;
import com.grow.member_service.achievement.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.achievement.challenge.domain.model.Challenge;
import com.grow.member_service.achievement.challenge.domain.repository.ChallengeRepository;
import com.grow.member_service.common.exception.AccomplishedException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.util.JsonUtils;
import com.grow.member_service.history.point.application.event.PointGrantRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccomplishedApplicationServiceImpl implements AccomplishedApplicationService {

	private static final String POINT_GRANT_TOPIC = "point.grant.requested";
	private final AccomplishedRepository repo;
	private final ChallengeRepository challengeRepo;
	private final ChallengeRepository challengeRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	/**
	 * 업적 달성
	 * 1) challengeId 유효성 검증
	 * 2) (memberId, challengeId) 중복 방지
	 * 3) 저장 성공 후 포인트 지급 커맨드 Kafka 퍼블리시
	 */
	@Override
	@Transactional
	public AccomplishedResponse createAccomplishment(Long memberId, CreateAccomplishedRequest req) {
		// 1) 챌린지 검증
		Challenge challenge = challengeRepo.findById(req.getChallengeId())
			.orElseThrow(() -> new AccomplishedException(ErrorCode.CHALLENGE_NOT_FOUND));

		// 2) 중복 가드(읽기)
		Optional<Accomplished> existing = repo.findByMemberIdAndChallengeId(memberId, req.getChallengeId());
		if (existing.isPresent()) {
			return AccomplishedResponse.from(existing.get());
		}

		// 3) 저장
		Accomplished saved;
		try {
			saved = repo.save(new Accomplished(
				memberId,
				req.getChallengeId(),
				LocalDateTime.now()
			));
		} catch (DataIntegrityViolationException dupe) {
			Optional<Accomplished> again = repo.findByMemberIdAndChallengeId(memberId, req.getChallengeId());
			if (again.isPresent()) return AccomplishedResponse.from(again.get());
			throw new AccomplishedException(ErrorCode.ACCOMPLISHED_DUPLICATE, dupe);
		}

		log.info("[업적] 저장 완료: accomplishedId={}, memberId={}, challengeId={}",
			saved.getAccomplishedId(), memberId, req.getChallengeId());

		// 4) 포인트 지급 커맨드 Kafka 퍼블리시 (멱등키 포함)
		String dedup = "ACHV-" + req.getChallengeId() + "-MEM-" + memberId;
		PointGrantRequest cmd = new PointGrantRequest(
			memberId,
			challenge.getPoint(),                  // amount (+ 적립)
			"ACHIEVEMENT",                         // actionType
			"CHALLENGE",                           // sourceType
			String.valueOf(req.getChallengeId()),  // sourceId
			"[업적] " + challenge.getName(),        // content
			dedup,                                 // dedupKey
			saved.getAccomplishedAt()              // occurredAt
		);

		String key = memberId.toString();         // 같은 멤버는 같은 파티션으로
		String payload = JsonUtils.toJsonString(cmd);
		kafkaTemplate.send(POINT_GRANT_TOPIC, key, payload);
		log.info("[KAFKA][SENT] topic={}, key={}, memberId={}, challengeId={}, amount={}, action={}",
			POINT_GRANT_TOPIC, key, memberId, req.getChallengeId(), challenge.getPoint(), "ACHIEVEMENT");

		return AccomplishedResponse.from(saved);
	}

	/**
	 * 페이징·정렬 + (옵션)기간 필터링 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<AccomplishedResponse> searchAccomplishments(
		Long memberId,
		LocalDateTime startAt,
		LocalDateTime endAt,
		Pageable pageable
	) {
		Page<Accomplished> domainPage;
		if (startAt != null && endAt != null) {
			AccomplishedPeriod period = new AccomplishedPeriod(startAt, endAt);
			domainPage = repo.findByMemberIdAndAccomplishedAtBetween(
				memberId, period.getStartAt(), period.getEndAt(), pageable
			);
		} else {
			domainPage = repo.findByMemberId(memberId, pageable);
		}
		return domainPage.map(a -> {
			String name = challengeRepository.findById(a.getChallengeId())
				.map(Challenge::getName)
				.orElse("알 수 없는 업적");
			return AccomplishedResponse.from(a, name);
		});
	}
}