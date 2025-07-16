package com.grow.member_service.accomplished.application.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.accomplished.application.model.AccomplishedPeriod;
import com.grow.member_service.accomplished.domain.model.Accomplished;
import com.grow.member_service.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.common.exception.AccomplishedException;
import com.grow.member_service.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccomplishedApplicationService {

	private final AccomplishedRepository repo;


	/**
	 * 새 업적 달성
	 */
	@Transactional
	public AccomplishedResponse createAccomplishment(
		Long memberId,
		CreateAccomplishedRequest req
	) {
		// 중복 달성 방지
		repo.findByMemberIdAndChallengeId(memberId, req.getChallengeId())
			.ifPresent(a -> {
				throw new AccomplishedException(ErrorCode.REVIEW_ALREADY_EXISTS);
			});

		Accomplished domain = new Accomplished(
			memberId,
			req.getChallengeId(),
			LocalDateTime.now()
		);

		Accomplished saved = repo.save(domain);
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
				memberId,
				period.getStartAt(),
				period.getEndAt(),
				pageable
			);

			if (domainPage.isEmpty()) {
				throw new AccomplishedException(ErrorCode.ACCOMPLISHED_PERIOD_EMPTY);
			}
		} else {
			domainPage = repo.findByMemberId(memberId, pageable);
		}

		return domainPage.map(AccomplishedResponse::from);
	}
}