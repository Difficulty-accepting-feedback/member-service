package com.grow.member_service.achievement.trigger.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.achievement.accomplished.application.service.AccomplishedApplicationService;
import com.grow.member_service.achievement.trigger.event.AchievementTriggerEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementTriggerListener {

	private final AccomplishedApplicationService accomplishedService;

	/**
	 * 업적 달성 이벤트 수신
	 * @param e
	 */
	@EventListener
	public void on(AchievementTriggerEvent e) {
		try {
			accomplishedService.createAccomplishment(
				e.memberId(),
				new CreateAccomplishedRequest(e.challengeId())
			);
			log.info("[업적] 업적 생성됨: memberId={}, challengeId={}", e.memberId(), e.challengeId());
		} catch (Exception ex) {
			log.warn("[업적] 업적 생성 실패: memberId={}, challengeId={}, err={}",
				e.memberId(), e.challengeId(), ex.getMessage(), ex);
		}
	}
}