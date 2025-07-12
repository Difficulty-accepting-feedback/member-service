package com.grow.member_service.quiz.result.domain.exception;

import com.grow.member_service.common.exception.DomainException;

public class QuizResultDomainException extends DomainException {
	public QuizResultDomainException(String message) {
		super(message);
	}

	public static QuizResultDomainException InvalidMemberId() {
		return new QuizResultDomainException("유효한 멤버 ID가 필요합니다.");
	}

	public static QuizResultDomainException invalidQuizId() {
		return new QuizResultDomainException("유효한 퀴즈 ID가 필요합니다.");
	}

	public static QuizResultDomainException nullCorrectness() {
		return new QuizResultDomainException("정답 여부는 null일 수 없습니다.");
	}
}