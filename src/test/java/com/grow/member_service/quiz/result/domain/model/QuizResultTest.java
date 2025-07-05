package com.grow.member_service.quiz.result.domain.model;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuizResultTest {

	@Test
	@DisplayName("memberId가 null이면 IllegalArgumentException 발생")
	void constructor_nullMember_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> new QuizResult(null, 1L, true))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("유효한 멤버 ID");
	}

	@Test
	@DisplayName("quizId가 null이면 IllegalArgumentException 발생")
	void constructor_nullQuiz_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> new QuizResult(1L, null, true))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("유효한 퀴즈 ID");
	}

	@Test
	@DisplayName("isCorrect가 null이면 IllegalArgumentException 발생")
	void constructor_nullIsCorrect_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> new QuizResult(1L, 1L, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("정답 여부는 null");
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