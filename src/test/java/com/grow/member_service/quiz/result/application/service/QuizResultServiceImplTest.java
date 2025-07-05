package com.grow.member_service.quiz.result.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;
import java.util.List;
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
	@DisplayName("recordResult(): repository.save() 호출 후 도메인 객체 반환")
	void recordResult_callsSave_andReturnsDomain() {
		QuizResult returned = new QuizResult(1L, 10L, true);
		given(repository.save(any(QuizResult.class))).willReturn(returned);

		QuizResult result = service.recordResult(1L, 10L, true);

		then(repository).should().save(any(QuizResult.class));
		assertThat(result).isSameAs(returned);
	}

	@Test
	@DisplayName("getResultsForMember(): repository.findByMemberId() 연동 확인")
	void getResultsForMember_delegatesToRepository() {
		List<QuizResult> list = List.of(new QuizResult(1L,1L,true));
		given(repository.findByMemberId(1L)).willReturn(list);

		List<QuizResult> result = service.getResultsForMember(1L);

		then(repository).should().findByMemberId(1L);
		assertThat(result).isEqualTo(list);
	}

	@Test
	@DisplayName("countCorrectAnswers(): statisticsService 호출 검증")
	void countCorrectAnswers_usesStatisticsService() {
		List<QuizResult> list = List.of(
			new QuizResult(1L,1L,true),
			new QuizResult(1L,1L,false)
		);
		given(repository.findByMemberId(1L)).willReturn(list);
		given(statisticsService.countCorrect(list)).willReturn(1L);

		long count = service.countCorrectAnswers(1L);

		assertThat(count).isEqualTo(1L);
		then(statisticsService).should().countCorrect(list);
	}

	@Test
	@DisplayName("getSuccessRate(): statisticsService.successRate() 호출 검증")
	void getSuccessRate_usesStatisticsService() {
		List<QuizResult> list = List.of(new QuizResult(1L,1L,true));
		given(repository.findByMemberId(1L)).willReturn(list);
		given(statisticsService.successRate(list)).willReturn(1.0);

		double rate = service.getSuccessRate(1L);

		assertThat(rate).isEqualTo(1.0);
		then(statisticsService).should().successRate(list);
	}
}
