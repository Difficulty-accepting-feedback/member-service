package com.grow.member_service.achievement.accomplished.infra.persistence.event;

import static org.springframework.transaction.event.TransactionPhase.*;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.grow.member_service.achievement.accomplished.application.event.AchievementAchievedEvent;
import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEventHandler {

	private final PointCommandService pointCommandService;

	/**
	 * 업적 달성 이벤트를 처리합니다.
	 * 업적이 커밋되면 포인트 지급 이벤트를 발행해
	 * 기존 포인트 서비스가 그대로 처리하게 합니다.
	 * @param e 업적 달성 이벤트 객체
	 */
	@Async
	@TransactionalEventListener(phase = AFTER_COMMIT)
	public void on(AchievementAchievedEvent e) {
		log.info("[업적] 이벤트 수신: memberId={}, challengeId={}, reward={}", e.memberId(), e.challengeId(), e.rewardPoint());

		String dedup = (e.dedupKey() != null && !e.dedupKey().isBlank())
			? e.dedupKey()
			: ("ACHV-" + e.challengeId() + "-MEM-" + e.memberId());
		LocalDateTime when = (e.occurredAt() != null ? e.occurredAt() : LocalDateTime.now());

		try {
			pointCommandService.grant(
				e.memberId(),
				e.rewardPoint(),
				PointActionType.ACHIEVEMENT,  // ← enum에 실제 존재해야 함
				SourceType.CHALLENGE,         // ← enum에 실제 존재해야 함
				String.valueOf(e.challengeId()),
				"[업적] " + e.challengeName(),
				dedup,
				when
			);
			log.info("[업적] 포인트 적립 호출 완료: memberId={}, reward={}", e.memberId(), e.rewardPoint());
		} catch (Exception ex) {
			log.error("[업적] 포인트 적립 실패: memberId={}, challengeId={}, err={}",
				e.memberId(), e.challengeId(), ex.toString(), ex);
		}
	}
}