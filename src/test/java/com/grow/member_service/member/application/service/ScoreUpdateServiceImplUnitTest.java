package com.grow.member_service.member.application.service;

import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.grow.member_service.member.application.service.impl.ScoreUpdateServiceImpl;
import com.grow.member_service.member.domain.model.MemberScoreInfo;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class ScoreUpdateServiceImplUnitTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	RedisTemplate<String, Double> redisTemplate;

	@Mock
	ValueOperations<String, Double> valueOps;

	ScoreUpdateServiceImpl service;

	private static final String TRUST_KEY = ScoreUpdateServiceImpl.TRUST_KEY;
	private final Long MEMBER_ID = 999L;

	@BeforeEach
	void init() {
		// 직접 생성자를 통해 주입
		service = new ScoreUpdateServiceImpl(memberRepository, redisTemplate);
		// opsForValue() 가 valueOps 를 반환하도록 설정
		given(redisTemplate.opsForValue()).willReturn(valueOps);
	}

	@Test
	@DisplayName("saveMemberScoreToRedis: 기존 값 없으면 SET 호출")
	void saveMemberScore_new_shouldSet() {
		given(valueOps.get(TRUST_KEY + MEMBER_ID)).willReturn(null);

		service.saveMemberScoreToRedis(MEMBER_ID, 42.0);

		then(valueOps).should().set(TRUST_KEY + MEMBER_ID, 42.0);
	}

	@Test
	@DisplayName("saveMemberScoreToRedis: 기존 값과 같으면 SET 미호출")
	void saveMemberScore_same_shouldSkip() {
		given(valueOps.get(TRUST_KEY + MEMBER_ID)).willReturn(7.5);

		service.saveMemberScoreToRedis(MEMBER_ID, 7.5);

		then(valueOps).should(never()).set(anyString(), anyDouble());
	}

	@Test
	@DisplayName("saveMemberScoreToRedis: 기존 값 다르면 SET 호출")
	void saveMemberScore_diff_shouldUpdate() {
		given(valueOps.get(TRUST_KEY + MEMBER_ID)).willReturn(1.1);

		service.saveMemberScoreToRedis(MEMBER_ID, 9.9);

		then(valueOps).should().set(TRUST_KEY + MEMBER_ID, 9.9);
	}

	@Test
	@DisplayName("retryFailedUpdates: 첫 시도 성공 시 1회만 SET 호출")
	void retryFailed_firstSuccess() throws Exception {
		MemberScoreInfo info = new MemberScoreInfo(MEMBER_ID, 3.14);
		ConcurrentLinkedQueue<MemberScoreInfo> q = new ConcurrentLinkedQueue<>(List.of(info));

		// 전체 호출이 곧 valueOps.set() 을 실행 → 첫 시도 성공
		var m = ScoreUpdateServiceImpl.class
			.getDeclaredMethod("retryFailedUpdates", ConcurrentLinkedQueue.class);
		m.setAccessible(true);
		m.invoke(service, q);

		then(valueOps).should(times(1)).set(TRUST_KEY + MEMBER_ID, 3.14);
	}

	@Test
	@DisplayName("retryFailedUpdates: 모두 실패 시 3회 SET 호출")
	void retryFailed_allFail() throws Exception {
		MemberScoreInfo info = new MemberScoreInfo(MEMBER_ID, 2.71);
		ConcurrentLinkedQueue<MemberScoreInfo> q = new ConcurrentLinkedQueue<>(List.of(info));

		// saveMemberScoreToRedis 내부에서 valueOps.set() → 모두 예외
		willThrow(new RuntimeException("boom"))
			.given(valueOps).set(TRUST_KEY + MEMBER_ID, 2.71);

		var m = ScoreUpdateServiceImpl.class
			.getDeclaredMethod("retryFailedUpdates", ConcurrentLinkedQueue.class);
		m.setAccessible(true);
		m.invoke(service, q);

		then(valueOps).should(times(3)).set(TRUST_KEY + MEMBER_ID, 2.71);
	}

	@Test
	@DisplayName("updateAllMemberScores: 실패 2회 후 3회차 성공")
	void updateAllMemberScores_retryScenario() {
		MemberScoreInfo info = new MemberScoreInfo(MEMBER_ID, 5.55);
		given(memberRepository.findAllScore()).willReturn(List.of(info));

		AtomicInteger cnt = new AtomicInteger();
		// 첫 두 번은 예외, 세 번째는 정상 동작
		doAnswer(inv -> {
			if (cnt.incrementAndGet() < 3) throw new RuntimeException("err");
			return null; // set 성공
		}).when(valueOps).set(TRUST_KEY + MEMBER_ID, 5.55);

		service.updateAllMemberScores();

		then(valueOps).should(times(3)).set(TRUST_KEY + MEMBER_ID, 5.55);
	}
}