package com.grow.member_service.history.point.application.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointCommandServiceImpl implements PointCommandService {

	private final MemberRepository memberRepository;
	private final PointHistoryRepository historyRepository;

	/**
	 * 포인트 적립 (양수만 허용)
	 */
	@Override
	@Transactional
	public PointHistory grant(Long memberId,
		int amount,
		PointActionType actionType,
		SourceType sourceType,
		String sourceId,
		String content,
		String dedupKey,
		LocalDateTime occurredAt) {

		if (amount <= 0) {
			throw new PointHistoryException(ErrorCode.POINT_AMOUNT_MUST_BE_POSITIVE);
		}

		LocalDateTime when = (occurredAt != null ? occurredAt : LocalDateTime.now());

		// 멱등: dedupKey가 있으면 먼저 조회
		if (dedupKey != null) {
			Optional<PointHistory> existed = historyRepository.findByDedupKey(dedupKey);
			if (existed.isPresent()) return existed.get();
		}

		int retries = 0;
		while (true) {
			try {
				Member member = memberRepository.findById(memberId)
					.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

				// 잔액 변경 (적립)
				member.addPoint(amount);

				// 히스토리 생성 (거래 후 잔액 포함)
				long after = member.getTotalPoint();
				PointHistory ph = new PointHistory(
					null,
					memberId,
					Integer.valueOf(amount),     // 적립은 양수
					content,
					when,
					actionType,
					sourceType,
					sourceId,
					dedupKey,
					Long.valueOf(after)
				);

				// 저장 (멤버 → 히스토리 순)
				memberRepository.save(member);
				try {
					return historyRepository.save(ph);
				} catch (DataIntegrityViolationException dup) {
					// dedup UNIQUE 충돌 -> 멱등 반환
					return historyRepository.findByDedupKey(dedupKey)
						.orElseThrow(() -> dup);
				}

			} catch (ObjectOptimisticLockingFailureException e) {
				retries++;
				if (retries >= 3) throw e;
			}
		}
	}

	/**
	 * 포인트 차감 (amount는 양수로 받고, 히스토리는 음수로 기록)
	 */
	@Override
	@Transactional
	public PointHistory spend(Long memberId,
		int amount,
		PointActionType actionType,
		SourceType sourceType,
		String sourceId,
		String content,
		String dedupKey,
		LocalDateTime occurredAt) {

		if (amount <= 0) {
			throw new PointHistoryException(ErrorCode.POINT_AMOUNT_MUST_BE_POSITIVE);
		}

		LocalDateTime when = (occurredAt != null ? occurredAt : LocalDateTime.now());

		// 멱등
		if (dedupKey != null) {
			Optional<PointHistory> existed = historyRepository.findByDedupKey(dedupKey);
			if (existed.isPresent()) return existed.get();
		}

		int retries = 0;
		while (true) {
			try {
				Member member = memberRepository.findById(memberId)
					.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

				// 차감 도메인 규칙 (잔액 검증 포함)
				// - Member.deductPoint(amount) 메서드가 있어야 함
				member.deductPoint(amount);

				// 히스토리 생성 (음수 금액, 거래 후 잔액)
				long after = member.getTotalPoint();
				PointHistory ph = new PointHistory(
					null,
					memberId,
					Integer.valueOf(-amount),    // 차감은 음수로 기록
					content,
					when,
					actionType,
					sourceType,
					sourceId,
					dedupKey,
					Long.valueOf(after)
				);

				// 저장
				memberRepository.save(member);
				try {
					return historyRepository.save(ph);
				} catch (DataIntegrityViolationException dup) {
					return historyRepository.findByDedupKey(dedupKey)
						.orElseThrow(() -> dup);
				}

			} catch (ObjectOptimisticLockingFailureException e) {
				retries++;
				if (retries >= 3) throw e;
			}
		}
	}
}