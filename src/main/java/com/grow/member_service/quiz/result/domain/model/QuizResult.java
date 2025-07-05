package com.grow.member_service.quiz.result.domain.model;

import com.grow.member_service.quiz.result.domain.exception.QuizResultDomainException;

import lombok.Getter;

@Getter
public class QuizResult {

    private final Long quizResultId;
    private final Long memberId; // 멤버 ID
    private final Long quizId; // 퀴즈 (실제 문제) ID
    private Boolean isCorrect; // 문제의 정답 여부

    public QuizResult(Long memberId,
                      Long quizId,
                      Boolean isCorrect
    ) {

        if (memberId == null) {
            throw QuizResultDomainException.InvalidMemberId();
        }
        if (quizId == null) {
            throw QuizResultDomainException.invalidQuizId();
        }
        if (isCorrect == null) {
            throw QuizResultDomainException.nullCorrectness();
        }

        this.quizResultId = null;
        this.memberId = memberId;
        this.quizId = quizId;
        this.isCorrect = isCorrect;
    }

    public QuizResult(Long quizResultId,
                      Long memberId,
                      Long quizId,
                      Boolean isCorrect
    ) {
        this.quizResultId = quizResultId;
        this.memberId = memberId;
        this.quizId = quizId;
        this.isCorrect = isCorrect;
    }

    public void markCorrect()   {
        this.isCorrect = true;
    }

    public void markIncorrect() {
        this.isCorrect = false;
    }
}