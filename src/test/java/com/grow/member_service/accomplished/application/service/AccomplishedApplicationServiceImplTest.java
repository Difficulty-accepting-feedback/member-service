package com.grow.member_service.accomplished.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import com.grow.member_service.achievement.accomplished.application.dto.AccomplishedResponse;
import com.grow.member_service.achievement.accomplished.application.dto.CreateAccomplishedRequest;
import com.grow.member_service.achievement.accomplished.application.service.impl.AccomplishedApplicationServiceImpl;
import com.grow.member_service.achievement.accomplished.domain.model.Accomplished;
import com.grow.member_service.achievement.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.achievement.challenge.domain.model.Challenge;
import com.grow.member_service.achievement.challenge.domain.repository.ChallengeRepository;

@ExtendWith(MockitoExtension.class)
class AccomplishedApplicationServiceImplTest {

	@Mock private AccomplishedRepository repo;

	// 서비스에 동일 타입 필드가 2개 있으므로 둘 다 모킹 필요
	@Mock private ChallengeRepository challengeRepo;        // createAccomplishment()에서 사용
	@Mock private ChallengeRepository challengeRepository;  // searchAccomplishments()에서 사용

	@Mock private KafkaTemplate<String, String> kafkaTemplate;

	@InjectMocks
	private AccomplishedApplicationServiceImpl sut;

	private void stubKafkaSend() {
		@SuppressWarnings("unchecked")
		SendResult<String, String> sendResult = mock(SendResult.class);
		given(kafkaTemplate.send(anyString(), anyString(), anyString()))
			.willReturn(CompletableFuture.completedFuture(sendResult));
	}

	@BeforeEach
	void forceInject() {
		ReflectionTestUtils.setField(sut, "challengeRepo", challengeRepo);
		ReflectionTestUtils.setField(sut, "challengeRepository", challengeRepository);
	}

	@Test
	@DisplayName("업적 달성 성공 → 저장 & Kafka 이벤트 발행(업적명/포인트/멱등키 포함)")
	void createAccomplishment_success() {
		// given
		stubKafkaSend();

		Long memberId = 10L;
		Long challengeId = 2001L; // 예: 첫 출석
		CreateAccomplishedRequest req = new CreateAccomplishedRequest(challengeId);

		Challenge challenge = new Challenge(challengeId, "첫 출석", "처음으로 출석", 50);
		given(challengeRepo.findById(challengeId)).willReturn(Optional.of(challenge));
		given(repo.findByMemberIdAndChallengeId(memberId, challengeId)).willReturn(Optional.empty());

		LocalDateTime now = LocalDateTime.now();
		Accomplished saved = new Accomplished(1L, memberId, challengeId, now);
		given(repo.save(any(Accomplished.class))).willReturn(saved);

		// when
		AccomplishedResponse res = sut.createAccomplishment(memberId, req);

		// then: 응답 검증
		assertThat(res.getAccomplishedId()).isEqualTo(1L);
		assertThat(res.getChallengeId()).isEqualTo(challengeId);
		assertThat(res.getAccomplishedAt()).isNotNull();

		// Kafka 퍼블리시 검증
		ArgumentCaptor<String> topicCap = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCap   = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> payloadCap = ArgumentCaptor.forClass(String.class);

		then(kafkaTemplate).should(times(1)).send(
			topicCap.capture(),
			keyCap.capture(),
			payloadCap.capture()
		);

		assertThat(topicCap.getValue()).isEqualTo("achievement.achieved");
		assertThat(keyCap.getValue()).isEqualTo(memberId.toString());

		String payload = payloadCap.getValue();
		// payload는 AchievementAchievedEvent의 JSON 문자열 — 핵심 필드 포함 여부로 느슨 검증
		assertThat(payload).contains("\"accomplishedId\":1");
		assertThat(payload).contains("\"memberId\":" + memberId);
		assertThat(payload).contains("\"challengeId\":" + challengeId);
		assertThat(payload).contains("\"challengeName\":\"첫 출석\"");
		assertThat(payload).contains("\"rewardPoint\":50");
		assertThat(payload).contains("\"dedupKey\":\"ACHV-" + challengeId + "-MEM-" + memberId + "\"");
		assertThat(payload).contains("\"occurredAt\":\"" + now.toLocalDate());// 날짜 prefix 정도만

		// 저장/조회 호출 검증
		then(challengeRepo).should().findById(challengeId);
		then(repo).should().findByMemberIdAndChallengeId(memberId, challengeId);
		then(repo).should().save(any(Accomplished.class));
	}

	@Test
	@DisplayName("이미 달성한 업적이면 기존 레코드 반환(저장/카프카 발행 없음)")
	void createAccomplishment_duplicateReturnsExisting() {
		// given
		Long memberId = 10L;
		Long challengeId = 2001L;
		CreateAccomplishedRequest req = new CreateAccomplishedRequest(challengeId);

		Challenge challenge = new Challenge(challengeId, "첫 출석", "처음으로 출석", 50);
		given(challengeRepo.findById(challengeId)).willReturn(Optional.of(challenge));

		LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
		Accomplished existing = new Accomplished(99L, memberId, challengeId, createdAt);
		given(repo.findByMemberIdAndChallengeId(memberId, challengeId)).willReturn(Optional.of(existing));

		// when
		AccomplishedResponse res = sut.createAccomplishment(memberId, req);

		// then
		assertThat(res.getAccomplishedId()).isEqualTo(99L);
		assertThat(res.getChallengeId()).isEqualTo(challengeId);
		assertThat(res.getAccomplishedAt()).isEqualTo(createdAt);

		then(repo).should(never()).save(any());
		then(kafkaTemplate).should(never()).send(anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("기간 조회(존재) → 페이지 반환 (이름 resolve는 challengeRepository 사용)")
	void searchAccomplishments_withPeriod_ok() {
		// given
		Long memberId = 10L;
		LocalDateTime start = LocalDateTime.now().minusDays(7);
		LocalDateTime end   = LocalDateTime.now();
		PageRequest pageable = PageRequest.of(0, 20);

		Accomplished a1 = new Accomplished(1L, memberId, 2001L, start.plusDays(1));
		Page<Accomplished> page = new PageImpl<>(List.of(a1), pageable, 1);
		given(repo.findByMemberIdAndAccomplishedAtBetween(memberId, start, end, pageable)).willReturn(page);
		// 이름 resolve (없어도 동작은 하지만, 현실성 있게 스텁)
		given(challengeRepository.findById(2001L))
			.willReturn(Optional.of(new Challenge(2001L, "첫 출석", "처음으로 출석", 50)));

		// when
		Page<AccomplishedResponse> result = sut.searchAccomplishments(memberId, start, end, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent().get(0).getAccomplishedId()).isEqualTo(1L);
		assertThat(result.getContent().get(0).getChallengeId()).isEqualTo(2001L);
	}

	@Test
	@DisplayName("기간 조회(없음) → 빈 페이지 반환(예외 없음)")
	void searchAccomplishments_withPeriod_empty_ok() {
		// given
		Long memberId = 10L;
		LocalDateTime start = LocalDateTime.now().minusDays(7);
		LocalDateTime end   = LocalDateTime.now();
		PageRequest pageable = PageRequest.of(0, 20);

		given(repo.findByMemberIdAndAccomplishedAtBetween(memberId, start, end, pageable))
			.willReturn(Page.empty(pageable));

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
		given(repo.findByMemberId(memberId, pageable))
			.willReturn(new PageImpl<>(List.of(a1), pageable, 1));
		given(challengeRepository.findById(2001L))
			.willReturn(Optional.of(new Challenge(2001L, "첫 출석", "처음으로 출석", 50)));

		// when
		Page<AccomplishedResponse> result = sut.searchAccomplishments(memberId, null, null, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent().get(0).getChallengeId()).isEqualTo(2001L);
	}
}