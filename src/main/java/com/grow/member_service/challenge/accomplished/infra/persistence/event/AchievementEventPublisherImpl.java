package com.grow.member_service.challenge.accomplished.infra.persistence.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.grow.member_service.challenge.accomplished.application.event.AchievementAchievedEvent;
import com.grow.member_service.challenge.accomplished.application.event.AchievementEventPublisher;

@Component
public class AchievementEventPublisherImpl implements AchievementEventPublisher {

	private final ApplicationEventPublisher delegate;

	public AchievementEventPublisherImpl(ApplicationEventPublisher delegate) {
		this.delegate = delegate;
	}

	/**
	 * 이벤트를 발행합니다.
	 * @param event 발행할 이벤트 객체
	 */
	@Override
	public void publish(AchievementAchievedEvent event) {
		delegate.publishEvent(event);
	}
}