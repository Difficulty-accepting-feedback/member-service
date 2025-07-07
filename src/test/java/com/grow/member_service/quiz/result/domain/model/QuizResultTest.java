package com.grow.member_service.quiz.result.domain.model;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.quiz.result.domain.exception.QuizResultDomainException;

class QuizResultTest {

	@Test
	@DisplayName("memberId가 null이면 QuizResultDomainException 발생")
	void constructor_nullMember_throwsQuizResultDomainException() {
		assertThatThrownBy(() -> new QuizResult(null, 1L, true))
			.isInstanceOf(QuizResultDomainException.class)
			.hasMessageContaining("유효한 멤버 ID가 필요합니다.");
	}

	@Test
	@DisplayName("quizId가 null이면 QuizResultDomainException 발생")
	void constructor_nullQuiz_throwsQuizResultDomainException() {
		assertThatThrownBy(() -> new QuizResult(1L, null, true))
			.isInstanceOf(QuizResultDomainException.class)
			.hasMessageContaining("유효한 퀴즈 ID가 필요합니다.");
	}

	@Test
	@DisplayName("isCorrect가 null이면 QuizResultDomainException 발생")
	void constructor_nullIsCorrect_throwsQuizResultDomainException() {
		assertThatThrownBy(() -> new QuizResult(1L, 1L, null))
			.isInstanceOf(QuizResultDomainException.class)
			.hasMessageContaining("정답 여부는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("markCorrect(): isCorrect가 true로 변경되는지 확인")
	void markCorrect_setsTrue() {
		QuizResult r = new QuizResult(1L, 1L, false);
		r.markCorrect();
		assertThat(r.getIsCorrect()).isTrue();
	}

	@Test
	@DisplayName("markIncorrect(): isCorrect가 false로 변경되는지 확인")
	void markIncorrect_setsFalse() {
		QuizResult r = new QuizResult(1L, 1L, true);
		r.markIncorrect();
		assertThat(r.getIsCorrect()).isFalse();
	}
}