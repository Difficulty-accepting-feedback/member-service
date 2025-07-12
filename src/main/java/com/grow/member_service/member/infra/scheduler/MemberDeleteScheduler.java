package com.grow.member_service.member.infra.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;
import com.grow.member_service.member.infra.persistence.repository.MemberJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 *  매일 새벽 2시에 실행, 탈퇴한 지 30일이 넘은 유저를 물리 삭제하는 스케줄러
 */
@Component
@RequiredArgsConstructor
public class MemberDeleteScheduler {

	private final MemberJpaRepository memberJpaRepository;

	@Scheduled(cron = "0 0 2 * * *")
	@Transactional
	public void purgeWithdrawnMembers() {
		LocalDateTime threshold = LocalDateTime.now().minusDays(30);

		List<MemberJpaEntity> toDelete = memberJpaRepository.findAllByWithdrawalAtBefore(threshold);
		if (toDelete.isEmpty()) {
			return;
		}
		memberJpaRepository.deleteAllInBatch(toDelete);
	}
}