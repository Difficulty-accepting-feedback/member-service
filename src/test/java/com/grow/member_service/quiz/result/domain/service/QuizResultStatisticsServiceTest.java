package com.grow.member_service.quiz.result.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.util.List;
import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.infra.service.QuizResultStatisticsServiceImpl;

@DisplayName("QuizResultStatisticsService 순수 계산 로직 검증")
class QuizResultStatisticsServiceTest {

	// 도메인 계층의 구현체를 직접 생성
	private final QuizResultStatisticsService svc =
		new QuizResultStatisticsServiceImpl();

	@Test
	@DisplayName("countCorrect(): true 값만 정확히 세는지 확인")
	void countCorrect_countsOnlyTrue() {
		List<QuizResult> list = List.of(
			new QuizResult(1L,1L,true),
			new QuizResult(1L,1L,false),
			new QuizResult(1L,1L,true)
		);
		long count = svc.countCorrect(list);
		assertThat(count).isEqualTo(2);
	}

	@Test
	@DisplayName("successRate(): 빈 리스트일 때 0.0 반환")
	void successRate_emptyList_returnsZero() {
		assertThat(svc.successRate(List.of())).isEqualTo(0.0);
	}

	@Test
	@DisplayName("successRate(): 정상 리스트에서 올바른 비율 계산")
	void successRate_computesCorrectly() {
		List<QuizResult> list = List.of(
			new QuizResult(1L,1L,true),
			new QuizResult(1L,1L,false),
			new QuizResult(1L,1L,true)
		);
		assertThat(svc.successRate(list))
			.isCloseTo(2.0/3.0, within(1e-6));
	}
}
