package com.grow.member_service.challenge.accomplished.application.event;

public interface AchievementEventPublisher {
	void publish(AchievementAchievedEvent e);
}