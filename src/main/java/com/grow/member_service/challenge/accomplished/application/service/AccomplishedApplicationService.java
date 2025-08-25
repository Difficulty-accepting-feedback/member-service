package com.grow.member_service.challenge.accomplished.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.challenge.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.challenge.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.challenge.accomplished.application.event.AchievementAchievedEvent;
import com.grow.member_service.challenge.accomplished.application.event.AchievementEventPublisher;
import com.grow.member_service.challenge.accomplished.application.model.AccomplishedPeriod;
import com.grow.member_service.challenge.accomplished.domain.model.Accomplished;
import com.grow.member_service.challenge.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.challenge.challenge.domain.model.Challenge;
import com.grow.member_service.challenge.challenge.domain.repository.ChallengeRepository;
import com.grow.member_service.common.exception.AccomplishedException;
import com.grow.member_service.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccomplishedApplicationService {

	private final AccomplishedRepository repo;
	private final ChallengeRepository challengeRepo;
	private final AchievementEventPublisher achievementPublisher;

	/**
	 * 업적 달성
	 * 1) challengeId 유효성 검증
	 * 2) (memberId, challengeId) 중복 방지
	 * 3) 저장 성공 후 업적명/보상포인트를 포함한 이벤트 발행 (AFTER_COMMIT에서 포인트/알림 처리)
	 */
	@Transactional
	public AccomplishedResponse createAccomplishment(Long memberId, CreateAccomplishedRequest req) {
		// 1) 챌린지 검증
		Challenge challenge = challengeRepo.findById(req.getChallengeId())
			.orElseThrow(() -> new AccomplishedException(ErrorCode.NOT_FOUND));

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
			throw dupe;
		}

		// 4) 업적 달성 이벤트 발행(업적명/보상포인트 포함)
		String dedup = "ACHV-" + req.getChallengeId() + "-MEM-" + memberId;
		achievementPublisher.publish(new AchievementAchievedEvent(
			saved.getAccomplishedId(),
			memberId,
			req.getChallengeId(),
			challenge.getName(),
			challenge.getPoint(),
			saved.getAccomplishedAt(),
			dedup
		));

		return AccomplishedResponse.from(saved);
	}

	/**
	 * 페이징·정렬 + (옵션)기간 필터링 조회
	 */
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
		return domainPage.map(AccomplishedResponse::from);
	}
}