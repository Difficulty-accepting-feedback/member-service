package com.grow.member_service.history.point.application.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.common.exception.PointHistoryException;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class PointCommandServiceImplTest {

	@Mock
	MemberRepository memberRepository;

	@Mock
	PointHistoryRepository historyRepository;

	@InjectMocks
	PointCommandServiceImpl service;

	@Test
	@DisplayName("grant: 정상 적립 → Member.addPoint 호출, 히스토리 저장 및 반환")
	void grant_success() {
		Long memberId = 1L;
		int amount = 100;
		String dedupKey = "DUP:1";
		LocalDateTime when = LocalDateTime.of(2025, 8, 23, 10, 0);

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		// 적립 후 잔액 조회 값
		when(member.getTotalPoint()).thenReturn(150);

		// dedup 미존재
		when(historyRepository.findByDedupKey(dedupKey)).thenReturn(Optional.empty());
		// save 시 전달된 객체 그대로 반환
		when(historyRepository.save(any(PointHistory.class))).thenAnswer(inv -> inv.getArgument(0));

		PointHistory result = service.grant(
			memberId, amount,
			PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src-1", "관리자 적립", dedupKey, when
		);

		// 멤버 갱신/히스토리 저장 호출 확인
		verify(memberRepository).save(member);
		verify(historyRepository).save(any(PointHistory.class));
		verify(member).addPoint(amount);

		// 저장된 히스토리 값 캡처해 검증
		ArgumentCaptor<PointHistory> cap = ArgumentCaptor.forClass(PointHistory.class);
		verify(historyRepository).save(cap.capture());
		PointHistory saved = cap.getValue();
		assertThat(saved.getMemberId()).isEqualTo(memberId);
		assertThat(saved.getAmount()).isEqualTo(Integer.valueOf(100));
		assertThat(saved.getContent()).isEqualTo("관리자 적립");
		assertThat(saved.getActionType()).isEqualTo(PointActionType.ADMIN_ADJUST);
		assertThat(saved.getSourceType()).isEqualTo(SourceType.SYSTEM);
		assertThat(saved.getSourceId()).isEqualTo("src-1");
		assertThat(saved.getDedupKey()).isEqualTo(dedupKey);
		assertThat(saved.getBalanceAfter()).isEqualTo(150L);

		// 결과도 동일해야 함
		assertThat(result.getBalanceAfter()).isEqualTo(150L);
	}

	@Test
	@DisplayName("grant: dedupKey가 이미 존재하면 저장 없이 기존 히스토리 반환")
	void grant_dedup_hit() {
		Long memberId = 1L;
		int amount = 100;
		String dedupKey = "DUP:EXIST";
		LocalDateTime when = LocalDateTime.of(2025, 8, 23, 10, 0);

		PointHistory existed = mock(PointHistory.class);
		when(historyRepository.findByDedupKey(dedupKey)).thenReturn(Optional.of(existed));

		PointHistory result = service.grant(
			memberId, amount, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src-1", "관리자 적립", dedupKey, when
		);

		// 멤버 로드/저장, 히스토리 저장 모두 일어나지 않아야 함
		verify(memberRepository, never()).findById(any());
		verify(memberRepository, never()).save(any());
		verify(historyRepository, never()).save(any());
		assertThat(result).isSameAs(existed);
	}

	@Test
	@DisplayName("grant: 히스토리 저장 시 UNIQUE 충돌 → dedup 조회하여 기존 히스토리 반환")
	void grant_unique_conflict_then_return_dedup() {
		Long memberId = 1L;
		int amount = 100;
		String dedupKey = "DUP:CONFLICT";
		LocalDateTime when = LocalDateTime.of(2025, 8, 23, 10, 0);

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getTotalPoint()).thenReturn(200);

		when(historyRepository.findByDedupKey(dedupKey)).thenReturn(Optional.empty()) // 사전 조회는 비어있다가
			.thenReturn(Optional.of(mock(PointHistory.class))); // 충돌 후 재조회 시 존재

		when(historyRepository.save(any(PointHistory.class)))
			.thenThrow(new DataIntegrityViolationException("dup-key"));

		PointHistory result = service.grant(
			memberId, amount, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src-1", "관리자 적립", dedupKey, when
		);

		assertThat(result).isNotNull();
		verify(memberRepository).save(member);
		verify(historyRepository).save(any(PointHistory.class));
		// 충돌 후 dedup 재조회가 2번째로 호출됨
		verify(historyRepository, times(2)).findByDedupKey(dedupKey);
	}

	@Test
	@DisplayName("grant: 멤버가 없으면 예외")
	void grant_member_not_found() {
		Long memberId = 99L;
		when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.grant(
			memberId, 10, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src", "관리자 적립", "K", LocalDateTime.now()
		)).isInstanceOf(MemberException.class);
	}

	@Test
	@DisplayName("grant: amount <= 0 이면 예외")
	void grant_invalid_amount() {
		assertThatThrownBy(() -> service.grant(
			1L, 0, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src", "관리자 적립", "K", LocalDateTime.now()
		)).isInstanceOf(PointHistoryException.class);
	}

	@Test
	@DisplayName("spend: 정상 차감 → Member.deductPoint 호출, 히스토리에는 음수 금액으로 저장")
	void spend_success() {
		Long memberId = 2L;
		int amount = 70;
		String dedupKey = "SPEND:1";
		LocalDateTime when = LocalDateTime.of(2025, 8, 23, 11, 0);

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getTotalPoint()).thenReturn(30); // 차감 후 잔액

		when(historyRepository.findByDedupKey(dedupKey)).thenReturn(Optional.empty());
		when(historyRepository.save(any(PointHistory.class))).thenAnswer(inv -> inv.getArgument(0));

		PointHistory result = service.spend(
			memberId, amount, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src-2", "관리자 차감", dedupKey, when
		);

		verify(member).deductPoint(amount);
		ArgumentCaptor<PointHistory> cap = ArgumentCaptor.forClass(PointHistory.class);
		verify(historyRepository).save(cap.capture());
		PointHistory saved = cap.getValue();
		assertThat(saved.getAmount()).isEqualTo(Integer.valueOf(-amount)); // 음수 기록
		assertThat(saved.getBalanceAfter()).isEqualTo(30L);
		assertThat(result.getBalanceAfter()).isEqualTo(30L);
	}

	@Test
	@DisplayName("spend: 잔액 부족 등 도메인에서 예외 발생 시 그대로 전파")
	void spend_not_enough_balance() {
		Long memberId = 2L;
		int amount = 999;

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		// 차감 시 도메인 예외 발생
		doThrow(new PointHistoryException(com.grow.member_service.global.exception.ErrorCode.POINT_NOT_ENOUGH))
			.when(member).deductPoint(amount);

		assertThatThrownBy(() -> service.spend(
			memberId, amount, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src-2", "관리자 차감", "DK", LocalDateTime.now()
		)).isInstanceOf(PointHistoryException.class);

		verify(historyRepository, never()).save(any());
	}

	@Test
	@DisplayName("grant: 낙관락 1회 충돌 후 재시도 성공")
	void grant_retry_on_optimistic_lock() {
		Long memberId = 5L;
		int amount = 40;
		String dedupKey = "RETRY:1";
		LocalDateTime when = LocalDateTime.of(2025, 8, 23, 12, 0);

		Member member = mock(Member.class);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(member.getTotalPoint()).thenReturn(140);

		when(historyRepository.findByDedupKey(dedupKey)).thenReturn(Optional.empty());
		when(historyRepository.save(any(PointHistory.class))).thenAnswer(inv -> inv.getArgument(0));

		// ❌ doThrow().doNothing() 는 void 메서드 전용
		// ✅ 반환값이 있는 save(...)에는 thenThrow → thenAnswer(or thenReturn)
		when(memberRepository.save(any(Member.class)))
			.thenThrow(new ObjectOptimisticLockingFailureException(Member.class, memberId))
			.thenAnswer(inv -> inv.getArgument(0)); // 두 번째 호출부터는 정상 반환

		PointHistory result = service.grant(
			memberId, amount, PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
			"src", "관리자 적립", dedupKey, when
		);

		assertThat(result).isNotNull();
		verify(memberRepository, times(2)).save(any(Member.class)); // 1회 실패 + 1회 재시도
		verify(historyRepository).save(any(PointHistory.class));
	}
}