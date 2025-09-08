package com.grow.member_service.achievement.trigger;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * 이벤트 simpleName -> 카탈로그 ID 정적 매핑 레지스트리
 */
@Component
public class AchievementTriggerRegistry {


	private static final Map<String, Long> TRIGGERS = Map.of(
		"LoginSucceededEvent",           1001L,
		"PhoneVerifiedEvent",            1003L,
		"AddressSetEvent",               1004L,
		"FirstPaymentApprovedEvent",     4001L,
		"FirstSubscriptionActivatedEvent", 4002L
	);

	/**
	 * 이벤트 객체로부터 카탈로그 ID 조회
	 * @param eventType 이벤트 타입
	 * @return 카탈로그 ID (없으면 Optional.empty())
	 */
	public Optional<Long> resolveId(String eventType) {
		return Optional.ofNullable(TRIGGERS.get(eventType));
	}
}