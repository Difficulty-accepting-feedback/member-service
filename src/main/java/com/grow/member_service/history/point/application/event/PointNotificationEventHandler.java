package com.grow.member_service.history.point.application.event;

import static org.springframework.transaction.event.TransactionPhase.*;

import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.grow.member_service.member.infra.client.MemberNotificationClient;
import com.grow.member_service.member.infra.client.dto.MemberNotificationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointNotificationEventHandler {

	private final MemberNotificationClient notificationClient;
	private final ObjectProvider<StringRedisTemplate> redisProvider;

	private static final Duration DEDUPE_TTL = Duration.ofHours(24);

	@TransactionalEventListener(phase = AFTER_COMMIT)
	public void on(PointNotificationEvent e) {
		// 1) 알림 멱등(있으면 스킵)
		String keyPart  = (e.dedupKey() != null ? e.dedupKey() : String.valueOf(e.pointHistoryId()));
		String dedupeKey = "notify:point:" + keyPart;

		StringRedisTemplate redis = redisProvider.getIfAvailable();
		if (redis != null) {
			Boolean ok = redis.opsForValue().setIfAbsent(dedupeKey, "1", DEDUPE_TTL);
			if (!Boolean.TRUE.equals(ok)) {
				log.debug("[포인트][알림 전송 스킵] 중복 알림 건너뛰기 - key={}", dedupeKey);
				return;
			}
		}

		// 2) DTO 매핑(기존 DTO 재사용)
		boolean saved = e.amount() >= 0;
		String title   = saved ? "포인트 적립 안내" : "포인트 사용 안내";
		String body    = String.format("%s%d점 • 잔액 %d점 • 사유: %s",
			saved ? "+" : "-", Math.abs(e.amount()), e.balanceAfter(), e.content());

		MemberNotificationRequest req = MemberNotificationRequest.builder()
			.notificationType("POINT")
			.memberId(e.memberId())
			.title(title)
			.content(body)
			.code(e.actionType().name())
			.occurredAt(e.occurredAt())
			.build();

		// 3) 전송
		try {
			notificationClient.sendServiceNotice(req);
			log.info("[포인트][알림 전송] sent - memberId={}, histId={}, amount={}, balance={}",
				e.memberId(), e.pointHistoryId(), e.amount(), e.balanceAfter());
		} catch (Exception ex) {
			log.error("[포인트][알림 전송 실패] failed - memberId={}, histId={}, err={}",
				e.memberId(), e.pointHistoryId(), ex.toString(), ex);
		}
	}
}