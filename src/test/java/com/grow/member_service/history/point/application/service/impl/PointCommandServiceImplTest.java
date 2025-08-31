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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.history.point.application.event.PointNotificationEvent;
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
class PointCommandServiceImplTest {

	@Mock MemberRepository memberRepository;
	@Mock PointHistoryRepository historyRepository;
	@Mock ApplicationEventPublisher events;

	@InjectMocks
	PointCommandServiceImpl sut;

	private Member newMember(Long id, int totalPoint) {
		MemberProfile profile = new MemberProfile("u@test.com", "nick", null, Platform.KAKAO, "pid");
		MemberAdditionalInfo info = new MemberAdditionalInfo(null, "Seoul", true);
		// 생성자 시그니처는 현재 코드 기준. (출석 스냅샷 필드 버전이면 인자만 맞춰주세요)
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
		@DisplayName("적립 성공 → 히스토리 저장 + 이벤트 1회 발행")
		void grant_success_publishes_event() {
			// given
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

			// when
			PointHistory result = sut.grant(
				memberId, amount,
				PointActionType.DAILY_CHECK_IN, SourceType.ATTENDANCE,
				"2025-08-24", "출석 체크", dedup, when
			);

			// then
			assertThat(result.getPointHistoryId()).isEqualTo(10L);
			assertThat(result.getAmount()).isEqualTo(100);
			assertThat(result.getBalanceAfter()).isEqualTo(100L);

			ArgumentCaptor<PointNotificationEvent> evCap = ArgumentCaptor.forClass(PointNotificationEvent.class);
			verify(events, times(1)).publishEvent(evCap.capture());
			PointNotificationEvent ev = evCap.getValue();
			assertThat(ev.memberId()).isEqualTo(memberId);
			assertThat(ev.amount()).isEqualTo(100);
			assertThat(ev.balanceAfter()).isEqualTo(100L);
			assertThat(ev.actionType()).isEqualTo(PointActionType.DAILY_CHECK_IN);
			verify(historyRepository, times(1)).save(any(PointHistory.class));
		}

		@Test
		@DisplayName("멱등 선조회 hit → 기존 히스토리 반환, 저장/이벤트 없음")
		void grant_idempotent_hit_returns_existing_without_event() {
			// given
			Long memberId = 1L;
			String dedup = "K-dup";
			PointHistory existed = savedHistory(
				99L, memberId, 100, 200L,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM, "seed",
				"seed", dedup, LocalDateTime.now()
			);
			when(historyRepository.findByDedupKey(dedup)).thenReturn(Optional.of(existed));

			// when
			PointHistory result = sut.grant(
				memberId, 100,
				PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"seed", "seed", dedup, LocalDateTime.now()
			);

			// then
			assertThat(result.getPointHistoryId()).isEqualTo(99L);
			verify(historyRepository, never()).save(any());
			verify(memberRepository, never()).save(any());
			verify(events, never()).publishEvent(any());
		}

		@Test
		@DisplayName("히스토리 저장 UNIQUE 충돌 → 기존 히스토리 반환, 이벤트 미발행")
		void grant_unique_conflict_returns_existing() {
			// given
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
			verify(events, never()).publishEvent(any()); // 저장 성공이 아니라 충돌 → 이벤트 없음
		}

		@Test
		@DisplayName("낙관락 충돌 발생 시 재시도 후 성공")
		void grant_retry_on_optimistic_lock() {
			// given
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
			verify(events, times(1)).publishEvent(any(PointNotificationEvent.class));
			verify(memberRepository, times(3)).save(any()); // 두 번 실패 + 한 번 성공
		}

		@Test
		@DisplayName("amount ≤ 0 → 예외")
		void grant_amount_must_be_positive() {
			assertThatThrownBy(() -> sut.grant(
				1L, 0, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"s", "c", "k", LocalDateTime.now()
			)).isInstanceOf(PointHistoryException.class);
			verifyNoInteractions(memberRepository, historyRepository, events);
		}
	}

	@Nested
	class Spend {

		@Test
		@DisplayName("차감 성공 → 히스토리 저장 + 이벤트 1회 발행")
		void spend_success_publishes_event() {
			// given
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
			verify(events, times(1)).publishEvent(any(PointNotificationEvent.class));
		}

		@Test
		@DisplayName("멱등 선조회 hit → 기존 히스토리 반환, 저장/이벤트 없음")
		void spend_idempotent_hit_returns_existing_without_event() {
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
			verify(events, never()).publishEvent(any());
		}

		@Test
		@DisplayName("amount ≤ 0 → 예외")
		void spend_amount_must_be_positive() {
			assertThatThrownBy(() -> sut.spend(
				1L, 0, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
				"s", "c", "k", LocalDateTime.now()
			)).isInstanceOf(PointHistoryException.class);
			verifyNoInteractions(memberRepository, historyRepository, events);
		}
	}
}