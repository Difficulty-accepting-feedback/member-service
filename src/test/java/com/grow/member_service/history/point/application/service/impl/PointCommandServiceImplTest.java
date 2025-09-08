package com.grow.member_service.history.point.application.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PointCommandServiceImplTest {

	@Mock MemberRepository memberRepository;
	@Mock PointHistoryRepository historyRepository;

	@Mock KafkaTemplate<String, String> kafkaTemplate;
	@Mock ObjectProvider<StringRedisTemplate> redisProvider;

	@InjectMocks
	PointCommandServiceImpl sut;

	private Member newMember(Long id, int totalPoint) {
		MemberProfile profile = new MemberProfile("u@test.com", "nick", null, Platform.KAKAO, "pid");
		MemberAdditionalInfo info = new MemberAdditionalInfo(null, "Seoul", true);
		return new Member(id, profile, info, LocalDateTime.now(), totalPoint, 36.5, true);
	}

	private PointHistory savedHistory(Long histId, Long memberId, int amount, long balanceAfter,
		PointActionType action, SourceType src, String sourceId,
		String content, String dedup, LocalDateTime when) {
		return new PointHistory(
			histId, memberId, amount, content, when, action, src, sourceId, dedup, balanceAfter
		);
	}

	@Nested
	class Grant {

		@Test
		@DisplayName("적립 성공 → 히스토리 저장 + Kafka 전송 1회")
		void grant_success_publishes_kafka() {
			// Redis 멱등 스킵(=null)로 설정
			when(redisProvider.getIfAvailable()).thenReturn(null);

			Long memberId = 1L;
			int amount = 100;
			String dedup = "K-1";
			LocalDateTime when = LocalDateTime.now();

			Member m = newMember(memberId, 0);
			when(memberRepository.findById(memberId)).thenReturn(Optional.of(m));
			when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			PointHistory saved = savedHistory(
				10L, memberId, amount, 100L,
				PointActionType.DAILY_CHECK_IN, SourceType.ATTENDANCE, "2025-08-24",
				"출석 체크", dedup, when
			);
			when(historyRepository.findByDedupKey(dedup)).thenReturn(Optional.empty());
			when(historyRepository.save(any(PointHistory.class))).thenReturn(saved);

			PointHistory result = sut.grant(
				memberId, amount,
				PointActionType.DAILY_CHECK_IN, SourceType.ATTENDANCE,
				"2025-08-24", "출석 체크", dedup, when
			);

			assertThat(result.getPointHistoryId()).isEqualTo(10L);
			assertThat(result.getAmount()).isEqualTo(100);
			assertThat(result.getBalanceAfter()).isEqualTo(100L);
			verify(historyRepository, times(1)).save(any(PointHistory.class));

			// Kafka send 검증
			verify(kafkaTemplate, times(1))
				.send(eq("point.notification.requested"), eq(memberId.toString()), anyString());

			ArgumentCaptor<String> payloadCap = ArgumentCaptor.forClass(String.class);
			verify(kafkaTemplate).send(eq("point.notification.requested"), eq(memberId.toString()), payloadCap.capture());
			String payload = payloadCap.getValue();
			assertThat(payload).contains("\"memberId\":1");
			assertThat(payload).contains("\"notificationType\":\"POINT\"");
			assertThat(payload).contains("출석 체크");
		}

		@Test
		@DisplayName("멱등 선조회 hit → 기존 히스토리 반환, 저장/Kafka 전송 없음")
		void grant_idempotent_hit_returns_existing_without_kafka() {
			when(redisProvider.getIfAvailable()).thenReturn(null);

			Long memberId = 1L;
			String dedup = "K-dup";
			PointHistory existed = savedHistory(
				99L, memberId, 100, 200L,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM, "seed",
				"seed", dedup, LocalDateTime.now()
			);
			when(historyRepository.findByDedupKey(dedup)).thenReturn(Optional.of(existed));

			PointHistory result = sut.grant(
				memberId, 100,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"seed", "seed", dedup, LocalDateTime.now()
			);

			assertThat(result.getPointHistoryId()).isEqualTo(99L);
			verify(historyRepository, never()).save(any());
			verify(memberRepository, never()).save(any());
			verifyNoInteractions(kafkaTemplate); // Kafka 전송 없음
		}

		@Test
		@DisplayName("히스토리 저장 UNIQUE 충돌 → 기존 히스토리 반환, Kafka 전송 없음")
		void grant_unique_conflict_returns_existing() {
			when(redisProvider.getIfAvailable()).thenReturn(null);

			Long memberId = 1L;
			int amount = 100;
			String dedup = "K-unique";

			Member m = newMember(memberId, 0);
			when(memberRepository.findById(memberId)).thenReturn(Optional.of(m));
			when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			when(historyRepository.findByDedupKey(dedup)).thenReturn(Optional.empty())
				.thenReturn(Optional.of(savedHistory(
					7L, memberId, amount, 100L,
					PointActionType.ADMIN_ADJUST, SourceType.SYSTEM, "seed",
					"seed", dedup, LocalDateTime.now()
				)));
			when(historyRepository.save(any())).thenThrow(new DataIntegrityViolationException("dupe"));

			// when
			PointHistory result = sut.grant(
				memberId, amount,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"seed", "seed", dedup, LocalDateTime.now()
			);

			// then
			assertThat(result.getPointHistoryId()).isEqualTo(7L);
			verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
		}

		@Test
		@DisplayName("낙관락 충돌 발생 시 재시도 후 성공 → Kafka 전송 1회")
		void grant_retry_on_optimistic_lock() {
			when(redisProvider.getIfAvailable()).thenReturn(null);

			Long memberId = 1L;
			int amount = 50;
			String dedup = "K-retry";

			Member m = newMember(memberId, 0);
			when(memberRepository.findById(memberId)).thenReturn(Optional.of(m));
			when(memberRepository.save(any()))
				.thenThrow(new ObjectOptimisticLockingFailureException("Member", memberId))
				.thenThrow(new ObjectOptimisticLockingFailureException("Member", memberId))
				.thenAnswer(inv -> inv.getArgument(0));

			when(historyRepository.findByDedupKey(dedup)).thenReturn(Optional.empty());
			PointHistory saved = savedHistory(
				11L, memberId, amount, 50L,
				PointActionType.DAILY_CHECK_IN, SourceType.ATTENDANCE, "2025-08-24",
				"출석 체크", dedup, LocalDateTime.now()
			);
			when(historyRepository.save(any())).thenReturn(saved);

			// when
			PointHistory result = sut.grant(
				memberId, amount,
				PointActionType.DAILY_CHECK_IN, SourceType.ATTENDANCE,
				"2025-08-24", "출석 체크", dedup, LocalDateTime.now()
			);

			// then
			assertThat(result.getPointHistoryId()).isEqualTo(11L);
			verify(memberRepository, times(3)).save(any()); // 2회 실패 + 1회 성공
			verify(kafkaTemplate, times(1))
				.send(eq("point.notification.requested"), eq(memberId.toString()), anyString());
		}

		@Test
		@DisplayName("amount ≤ 0 → 예외")
		void grant_amount_must_be_positive() {
			assertThatThrownBy(() -> sut.grant(
				1L, 0, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"s", "c", "k", LocalDateTime.now()
			)).isInstanceOf(PointHistoryException.class);
			verifyNoInteractions(memberRepository, historyRepository, kafkaTemplate);
		}
	}

	@Nested
	class Spend {

		@Test
		@DisplayName("차감 성공 → 히스토리 저장 + Kafka 전송 1회")
		void spend_success_publishes_kafka() {
			when(redisProvider.getIfAvailable()).thenReturn(null);

			Long memberId = 1L;
			int start = 200;
			int use = 50;
			String dedup = "S-1";

			Member m = newMember(memberId, start);
			when(memberRepository.findById(memberId)).thenReturn(Optional.of(m));
			when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			PointHistory saved = savedHistory(
				20L, memberId, -use, start - use,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM, "seed",
				"사용", dedup, LocalDateTime.now()
			);
			when(historyRepository.findByDedupKey(dedup)).thenReturn(Optional.empty());
			when(historyRepository.save(any())).thenReturn(saved);

			// when
			PointHistory result = sut.spend(
				memberId, use,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"seed", "사용", dedup, LocalDateTime.now()
			);

			// then
			assertThat(result.getAmount()).isEqualTo(-50);
			assertThat(result.getBalanceAfter()).isEqualTo(150L);
			verify(kafkaTemplate, times(1))
				.send(eq("point.notification.requested"), eq(memberId.toString()), anyString());
		}

		@Test
		@DisplayName("멱등 선조회 hit → 기존 히스토리 반환, 저장/Kafka 전송 없음")
		void spend_idempotent_hit_returns_existing_without_kafka() {
			when(redisProvider.getIfAvailable()).thenReturn(null);

			Long memberId = 1L;
			String dedup = "S-dup";
			PointHistory existed = savedHistory(
				77L, memberId, -10, 90L,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM, "seed",
				"사용", dedup, LocalDateTime.now()
			);
			when(historyRepository.findByDedupKey(dedup)).thenReturn(Optional.of(existed));

			PointHistory result = sut.spend(
				memberId, 10,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"seed", "사용", dedup, LocalDateTime.now()
			);

			assertThat(result.getPointHistoryId()).isEqualTo(77L);
			verify(historyRepository, never()).save(any());
			verify(memberRepository, never()).save(any());
			verifyNoInteractions(kafkaTemplate);
		}

		@Test
		@DisplayName("amount ≤ 0 → 예외")
		void spend_amount_must_be_positive() {
			assertThatThrownBy(() -> sut.spend(
				1L, 0, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"s", "c", "k", LocalDateTime.now()
			)).isInstanceOf(PointHistoryException.class);
			verifyNoInteractions(memberRepository, historyRepository, kafkaTemplate);
		}
	}
}