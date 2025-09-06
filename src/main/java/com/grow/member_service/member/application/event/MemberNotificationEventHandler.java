// package com.grow.member_service.member.application.event;
//
// import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;
//
// import java.time.Duration;
//
// import org.springframework.beans.factory.ObjectProvider;
// import org.springframework.data.redis.core.StringRedisTemplate;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.event.TransactionalEventListener;
//
// import com.grow.member_service.member.infra.client.MemberNotificationClient;
// import com.grow.member_service.member.infra.client.dto.MemberNotificationRequest;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// /**
//  * GROW - 멤버 알림 이벤트 핸들러
//  * - 트랜잭션 커밋 이후(AFTER_COMMIT) Feign 호출
//  * - 온보딩 리마인더는 Redis로 12시간 중복 방지
//  */
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class MemberNotificationEventHandler {
//
// 	private final MemberNotificationClient memberNotificationClient;
// 	private final ObjectProvider<StringRedisTemplate> redisProvider;
//
// 	/** 12시간 중복 방지 TTL */
// 	private static final Duration DEDUPE_TTL = Duration.ofHours(12);
//
// 	/**
// 	 * 온보딩 리마인더 및 기타 알림 이벤트 처리
// 	 * @param e
// 	 */
// 	@TransactionalEventListener(phase = AFTER_COMMIT)
// 	public void on(MemberNotificationEvent e) {
// 		// 1) 온보딩 리마인더 중복 방지 (SETNX + TTL 12h)
// 		if (isReminder(e.code())) {
//
// 			// 중복 방지 키 생성
// 			String dedupeKey = "notify:onboard:" + e.code() + ":" + e.memberId();
//
// 			// 키가 이미 존재하면 스킵
// 			if (!acquireOnce(dedupeKey, DEDUPE_TTL)) {
// 				log.debug("[알림 스킵] 중복 방지 키(hit) - key={}, memberId={}, code={}", dedupeKey, e.memberId(), e.code());
// 				return;
// 			}
// 		}
//
// 		// 2) 요청 객체 생성
// 		MemberNotificationRequest req = MemberNotificationRequest.builder()
// 			.notificationType(e.type())
// 			.memberId(e.memberId())
// 			.title(e.title())
// 			.content(e.content())
// 			.code(e.code())
// 			.occurredAt(e.occurredAt())
// 			.build();
//
// 		// 3) 전송 (실패시 로깅만)
// 		try {
// 			memberNotificationClient.sendServiceNotice(req);
// 			log.info("[알림 전송] memberId={}, code={}, title='{}'", e.memberId(), e.code(), e.title());
// 		} catch (Exception ex) {
// 			log.error("[알림 전송 실패] memberId={}, code={}, err={}", e.memberId(), e.code(), ex.getMessage(), ex);
// 		}
// 	}
//
// 	/**
// 	 * 온보딩 리마인더 코드인지 확인
// 	 * @param code
// 	 * @return
// 	 */
// 	private boolean isReminder(String code) {
// 		return "ADDR_REMINDER".equals(code) || "PHONE_REMINDER".equals(code);
// 	}
//
// 	/** Redis SETNX로 12시간 동안 1회만 수행 */
// 	private boolean acquireOnce(String key, Duration ttl) {
// 		StringRedisTemplate r = redisProvider.getIfAvailable();
// 		if (r == null) return true; // 로컬/테스트: 바로 통과
// 		Boolean ok = r.opsForValue().setIfAbsent(key, "1", ttl);
// 		return Boolean.TRUE.equals(ok);
// 	}
// }