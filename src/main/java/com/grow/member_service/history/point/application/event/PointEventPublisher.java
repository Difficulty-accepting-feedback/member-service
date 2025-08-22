package com.grow.member_service.history.point.application.event;

public interface PointEventPublisher {
	void publish(PointGrantRequest event);
}