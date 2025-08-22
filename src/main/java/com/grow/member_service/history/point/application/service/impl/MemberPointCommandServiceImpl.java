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
import com.grow.member_service.history.point.application.service.MemberPointCommandService;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberPointCommandServiceImpl implements MemberPointCommandService {

	private final MemberRepository memberRepository;
	private final PointHistoryRepository historyRepository;
	// private final ApplicationEventPublisher events; // 필요 시 주입

	/**
	 * 포인트를 지급합니다.
	 * @param memberId 회원 ID
	 * @param amount 지급할 포인트 양 (양수는 적립, 음수는 차감 정책에 따라 허용)
	 * @param actionType 포인트 액션 타입
	 * @param sourceType 포인트 출처 타입 (예: ATTENDANCE, BOARD 등)
	 * @param sourceId 포인트 출처 ID (예: postId, 날짜 등)
	 * @param content 포인트 지급 사유 (예: "출석 체크", "게시글 작성" 등)
	 * @param dedupKey 멱등 키 (중복 지급 방지용, UNIQUE 제약 조건 적용)
	 * @param occurredAt 포인트 지급 발생 시각 (null이면 현재 시각 사용)
	 * @return 지급된 포인트 기록 객체
	 */
	@Override
	@Transactional
	public PointHistory grant(Long memberId, int amount,
		PointActionType actionType, SourceType sourceType, String sourceId,
		String content, String dedupKey, LocalDateTime occurredAt) {

		// 발생 시각 설정: null이면 현재 시각 사용
		LocalDateTime when = (occurredAt != null ? occurredAt : LocalDateTime.now());

		// 멱등 키가 주어진 경우, 이미 존재하는지 확인
		if (dedupKey != null) {
			Optional<PointHistory> existed = historyRepository.findByDedupKey(dedupKey);
			if (existed.isPresent()) return existed.get();
		}

		// 낙관적 락을 사용하여 멤버와 포인트 기록을 안전하게 업데이트합니다.
		int retries = 0;

		// 낙관적 락 충돌 시 재시도 로직
		while (true) {
			try {
				// 멤버 로드
				Member member = memberRepository.findById(memberId)
					.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

				// 잔액 변경 (음수 방지)
				if (amount < 0) {
					int use = Math.abs(amount);
					if (member.getTotalPoint() - use < 0) {
						throw new PointHistoryException(ErrorCode.POINT_NOT_ENOUGH);
					}
					member.addPoint(-use);
				} else {
					member.addPoint(amount);
				}

				// 히스토리 생성 (거래 후 잔액 포함)
				long after = member.getTotalPoint();
				PointHistory ph = new PointHistory(
					null, memberId, Integer.valueOf(amount), content, when,
					actionType, sourceType, sourceId, dedupKey, Long.valueOf(after)
				);

				// 저장 (낙관락으로 충돌 시 예외)
				memberRepository.save(member);
				try {
					return historyRepository.save(ph);
				} catch (DataIntegrityViolationException dup) {
					// dedup UNIQUE 충돌 -> 멱등 반환
					Optional<PointHistory> existed = historyRepository.findByDedupKey(dedupKey);
					return existed.orElseThrow(() -> dup);
				}

			} catch (ObjectOptimisticLockingFailureException e) {
				// 낙관락 충돌 -> 짧게 재시도
				retries++;
				if (retries >= 3) throw e;
			}
		}
	}
}