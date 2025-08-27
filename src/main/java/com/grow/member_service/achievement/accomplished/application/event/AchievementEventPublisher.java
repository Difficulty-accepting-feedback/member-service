package com.grow.member_service.achievement.accomplished.application.event;

public interface AchievementEventPublisher {
	void publish(AchievementAchievedEvent e);
}