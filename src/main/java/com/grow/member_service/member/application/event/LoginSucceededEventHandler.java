package com.grow.member_service.member.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.trigger.event.AchievementTriggerPublisher;
import com.grow.member_service.member.application.dto.AttendanceResult;
import com.grow.member_service.member.application.service.MemberAttendanceApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSucceededEventHandler {

	private final MemberAttendanceApplicationService attendanceService;
	private final AchievementTriggerPublisher achievementTriggerPublisher;
	/**
	 * 로그인 성공 시 출석 체크를 수행하고, 보상을 지급합니다.
	 * 출석 체크가 이미 완료된 경우에는 스킵합니다.
	 * @param e 로그인 성공 이벤트
	 */
	@EventListener
	public void on(LoginSucceededEvent e) {
		try {
			AttendanceResult result = attendanceService.checkInAndReward(e.memberId(), e.occurredAt()); // ★ var 제거
			if (result.attended()) {
				log.info("[Member] 로그인 출석 완료 - memberId={}, day={}, streak={}, best={}, granted={}, balanceAfter={}",
					e.memberId(), result.day(), result.streak(), result.bestStreak(),
					result.grantedAmount(), result.balanceAfter());

				// 첫 로그인 업적 처리
				boolean achieved = achievementTriggerPublisher.publishFirstLoginIfFirst(e.memberId());
				if (achieved) {
					log.info("[업적] 첫 로그인 업적 달성 - memberId={}", e.memberId());
				}

			} else {
				log.debug("[Member] 로그인 출석 스킵(이미 처리됨) - memberId={}, day={}", e.memberId(), result.day());
			}
		} catch (Exception ex) {
			log.warn("[Member] 로그인 출석 처리 실패 - memberId={}, err={}", e.memberId(), ex.toString());
		}
	}
}