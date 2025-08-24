package com.grow.member_service.history.point.infra.persistence.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.grow.member_service.history.point.application.event.PointEventPublisher;
import com.grow.member_service.history.point.application.event.PointGrantRequest;

@Component
public class PointEventPublisherImpl implements PointEventPublisher {

	private final ApplicationEventPublisher delegate;

	public PointEventPublisherImpl(ApplicationEventPublisher delegate) {
		this.delegate = delegate;
	}

	/**
	 * 이벤트를 발행합니다.
	 * @param event 발행할 이벤트 객체
	 */
	@Override
	public void publish(PointGrantRequest event) {
		delegate.publishEvent(event);
	}
}