package com.grow.member_service.challenge.accomplished.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.grow.member_service.challenge.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.challenge.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.challenge.accomplished.application.event.AchievementAchievedEvent;
import com.grow.member_service.challenge.accomplished.application.event.AchievementEventPublisher;
import com.grow.member_service.challenge.accomplished.domain.model.Accomplished;
import com.grow.member_service.challenge.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.challenge.challenge.domain.model.Challenge;
import com.grow.member_service.challenge.challenge.domain.repository.ChallengeRepository;

@ExtendWith(MockitoExtension.class)
class AccomplishedApplicationServiceTest {

	@Mock
	private AccomplishedRepository repo;

	@Mock
	private ChallengeRepository challengeRepo;

	@Mock
	private AchievementEventPublisher achievementPublisher;

	@InjectMocks
	private AccomplishedApplicationService sut; // System Under Test

	@Test
	@DisplayName("업적 달성 성공 → 저장 & 이벤트 발행(업적명/포인트 포함)")
	void createAccomplishment_success() {
		// given
		Long memberId = 10L;
		Long challengeId = 2001L; // 예: 첫 출석
		CreateAccomplishedRequest req = new CreateAccomplishedRequest(challengeId);

		Challenge challenge = new Challenge(challengeId, "첫 출석", "처음으로 출석", 50);
		when(challengeRepo.findById(challengeId)).thenReturn(Optional.of(challenge));
		when(repo.findByMemberIdAndChallengeId(memberId, challengeId)).thenReturn(Optional.empty());

		LocalDateTime now = LocalDateTime.now();
		Accomplished saved = new Accomplished(1L, memberId, challengeId, now);
		when(repo.save(any(Accomplished.class))).thenReturn(saved);

		// when
		AccomplishedResponse res = sut.createAccomplishment(memberId, req);

		// then
		assertThat(res.getAccomplishedId()).isEqualTo(1L);
		assertThat(res.getChallengeId()).isEqualTo(challengeId);
		assertThat(res.getAccomplishedAt()).isNotNull();

		// 이벤트 발행 검증
		ArgumentCaptor<AchievementAchievedEvent> captor = ArgumentCaptor.forClass(AchievementAchievedEvent.class);
		verify(achievementPublisher, times(1)).publish(captor.capture());

		AchievementAchievedEvent ev = captor.getValue();
		assertThat(ev.memberId()).isEqualTo(memberId);
		assertThat(ev.challengeId()).isEqualTo(challengeId);
		assertThat(ev.challengeName()).isEqualTo("첫 출석");
		assertThat(ev.rewardPoint()).isEqualTo(50);
		assertThat(ev.dedupKey()).isEqualTo("ACHV-" + challengeId + "-MEM-" + memberId);

		// 저장/조회 호출도 검증
		verify(challengeRepo).findById(challengeId);
		verify(repo).findByMemberIdAndChallengeId(memberId, challengeId);
		verify(repo).save(any(Accomplished.class));
	}

	@Test
	@DisplayName("이미 달성한 업적이면 예외 대신 기존 레코드 반환(저장/이벤트 없음)")
	void createAccomplishment_duplicateReturnsExisting() {
		// given
		Long memberId = 10L;
		Long challengeId = 2001L;
		CreateAccomplishedRequest req = new CreateAccomplishedRequest(challengeId);

		Challenge challenge = new Challenge(challengeId, "첫 출석", "처음으로 출석", 50);
		when(challengeRepo.findById(challengeId)).thenReturn(Optional.of(challenge));

		LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
		Accomplished existing = new Accomplished(99L, memberId, challengeId, createdAt);
		when(repo.findByMemberIdAndChallengeId(memberId, challengeId)).thenReturn(Optional.of(existing));

		// when
		AccomplishedResponse res = sut.createAccomplishment(memberId, req);

		// then
		assertThat(res.getAccomplishedId()).isEqualTo(99L);
		assertThat(res.getChallengeId()).isEqualTo(challengeId);
		assertThat(res.getAccomplishedAt()).isEqualTo(createdAt);

		verify(repo, never()).save(any());
		verify(achievementPublisher, never()).publish(any());
	}

	@Test
	@DisplayName("기간 조회(존재) → 페이지 반환")
	void searchAccomplishments_withPeriod_ok() {
		// given
		Long memberId = 10L;
		LocalDateTime start = LocalDateTime.now().minusDays(7);
		LocalDateTime end   = LocalDateTime.now();
		PageRequest pageable = PageRequest.of(0, 20);

		Accomplished a1 = new Accomplished(1L, memberId, 2001L, start.plusDays(1));
		Page<Accomplished> page = new PageImpl<>(List.of(a1), pageable, 1);
		when(repo.findByMemberIdAndAccomplishedAtBetween(memberId, start, end, pageable)).thenReturn(page);

		// when
		Page<AccomplishedResponse> result = sut.searchAccomplishments(memberId, start, end, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent().get(0).getAccomplishedId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("기간 조회(없음) → 빈 페이지 반환(예외 없음)")
	void searchAccomplishments_withPeriod_empty_ok() {
		// given
		Long memberId = 10L;
		LocalDateTime start = LocalDateTime.now().minusDays(7);
		LocalDateTime end   = LocalDateTime.now();
		PageRequest pageable = PageRequest.of(0, 20);

		when(repo.findByMemberIdAndAccomplishedAtBetween(memberId, start, end, pageable))
			.thenReturn(Page.empty(pageable));

		// when
		Page<AccomplishedResponse> result = sut.searchAccomplishments(memberId, start, end, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(0);
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	@DisplayName("기간 미지정 → 전체 조회")
	void searchAccomplishments_withoutPeriod_ok() {
		// given
		Long memberId = 10L;
		PageRequest pageable = PageRequest.of(0, 20);

		Accomplished a1 = new Accomplished(1L, memberId, 2001L, LocalDateTime.now());
		when(repo.findByMemberId(memberId, pageable))
			.thenReturn(new PageImpl<>(List.of(a1), pageable, 1));

		// when
		Page<AccomplishedResponse> result = sut.searchAccomplishments(memberId, null, null, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent().get(0).getChallengeId()).isEqualTo(2001L);
	}
}