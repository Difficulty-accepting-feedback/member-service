package com.grow.member_service.challenge.accomplished.infra.persistence.event;

import static org.springframework.transaction.event.TransactionPhase.*;

import java.time.LocalDateTime;

import org.springframework.transaction.event.TransactionalEventListener;

import com.grow.member_service.challenge.accomplished.application.event.AchievementAchievedEvent;
import com.grow.member_service.history.point.application.event.PointEventPublisher;
import com.grow.member_service.history.point.application.event.PointGrantRequest;

public class AchievementEventHandler {

	private final PointEventPublisher pointEventPublisher;

	public AchievementEventHandler(PointEventPublisher pointEventPublisher) {
		this.pointEventPublisher = pointEventPublisher;
	}

	/**
	 * 업적 달성 이벤트를 처리합니다.
	 * 업적이 커밋되면 포인트 지급 이벤트를 발행해
	 * 기존 포인트 핸들러가 그대로 처리하게 합니다.
	 * @param e 업적 달성 이벤트 객체
	 */
	@TransactionalEventListener(phase = AFTER_COMMIT)
	public void on(AchievementAchievedEvent e) {
		if (e.rewardPoint() <= 0) return; // 포인트 0이면 스킵

		LocalDateTime when = (e.occurredAt() != null ? e.occurredAt() : LocalDateTime.now());

		pointEventPublisher.publish(new PointGrantRequest(
			e.memberId(),
			e.rewardPoint(),           // +적립
			"ACHIEVEMENT",             // PointActionType.valueOf("ACHIEVEMENT")
			"CHALLENGE",               // SourceType.valueOf("CHALLENGE")
			String.valueOf(e.challengeId()),
			"[업적] " + e.challengeName(),
			// 멱등키-> 업적 기준으로 생성
			(e.dedupKey() != null && !e.dedupKey().isBlank())
				? e.dedupKey()
				: ("ACHV-" + e.challengeId() + "-MEM-" + e.memberId()),
			when
		));
	}
}