package com.grow.member_service.achievement.accomplished.infra.persistence.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.accomplished.application.event.AchievementAchievedEvent;
import com.grow.member_service.achievement.accomplished.application.event.AchievementEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEventPublisherImpl implements AchievementEventPublisher {

	private final ApplicationEventPublisher delegate;

	@Override
	public void publish(AchievementAchievedEvent event) {
		log.info("[업적] 달성 이벤트 발행: memberId={}, challengeId={}, point={}",
			event.memberId(), event.challengeId(), event.rewardPoint());
		delegate.publishEvent(event);
	}
}