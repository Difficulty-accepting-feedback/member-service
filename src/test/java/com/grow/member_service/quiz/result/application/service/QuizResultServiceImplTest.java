package com.grow.member_service.quiz.result.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.grow.member_service.common.exception.QuizResultException;
import com.grow.member_service.quiz.result.application.service.impl.QuizResultServiceImpl;
import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.repository.QuizResultRepository;
import com.grow.member_service.quiz.result.domain.service.QuizResultStatisticsService;

@DisplayName("QuizResultServiceImpl 유스케이스 흐름 검증")
class QuizResultServiceImplTest {

	@Mock QuizResultRepository repository;
	@Mock QuizResultStatisticsService statisticsService;
	@InjectMocks QuizResultServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("recordResult(): upsert 호출 후 findOne 결과 반환")
	void recordResult_callsUpsert_andReturnsFindOne() {
		// given
		Long memberId = 1L;
		Long quizId = 10L;
		boolean isCorrect = true;

		willDoNothing().given(repository).upsert(memberId, quizId, isCorrect);
		QuizResult returned = new QuizResult(100L, memberId, quizId, true); // (quizResultId, memberId, quizId, isCorrect)
		given(repository.findOne(memberId, quizId)).willReturn(Optional.of(returned));

		// when
		QuizResult result = service.recordResult(memberId, quizId, isCorrect);

		// then
		then(repository).should().upsert(memberId, quizId, isCorrect);
		then(repository).should().findOne(memberId, quizId);
		assertThat(result).isSameAs(returned);
	}

	@Test
	@DisplayName("recordResult(): isCorrect == null 이면 예외")
	void recordResult_whenIsCorrectNull_throws() {
		assertThatThrownBy(() -> service.recordResult(1L, 10L, null))
			.isInstanceOf(QuizResultException.class);
		then(repository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("getResultsForMember(): repository.findByMemberId() 연동 확인")
	void getResultsForMember_delegatesToRepository() {
		Long memberId = 1L;
		List<QuizResult> list = List.of(new QuizResult(101L, memberId, 1L, true));
		given(repository.findByMemberId(memberId)).willReturn(list);

		List<QuizResult> result = service.getResultsForMember(memberId);

		then(repository).should().findByMemberId(memberId);
		assertThat(result).isEqualTo(list);
	}

	@Test
	@DisplayName("getResultsForMember(): 비어있으면 예외")
	void getResultsForMember_empty_throws() {
		Long memberId = 1L;
		given(repository.findByMemberId(memberId)).willReturn(List.of());

		assertThatThrownBy(() -> service.getResultsForMember(memberId))
			.isInstanceOf(QuizResultException.class);
	}

	@Test
	@DisplayName("countCorrectAnswers(): statisticsService.countCorrect() 호출")
	void countCorrectAnswers_usesStatisticsService() {
		Long memberId = 1L;
		List<QuizResult> list = List.of(
			new QuizResult(201L, memberId, 1L, true),
			new QuizResult(202L, memberId, 2L, false)
		);
		given(repository.findByMemberId(memberId)).willReturn(list);
		given(statisticsService.countCorrect(list)).willReturn(1L);

		long count = service.countCorrectAnswers(memberId);

		assertThat(count).isEqualTo(1L);
		then(statisticsService).should().countCorrect(list);
	}

	@Test
	@DisplayName("getSuccessRate(): statisticsService.successRate() 호출")
	void getSuccessRate_usesStatisticsService() {
		Long memberId = 1L;
		List<QuizResult> list = List.of(new QuizResult(301L, memberId, 1L, true));
		given(repository.findByMemberId(memberId)).willReturn(list);
		given(statisticsService.successRate(list)).willReturn(1.0);

		double rate = service.getSuccessRate(memberId);

		assertThat(rate).isEqualTo(1.0);
		then(statisticsService).should().successRate(list);
	}
}