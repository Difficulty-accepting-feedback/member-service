package com.grow.member_service.quiz.result.domain.model;

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
            throw new IllegalArgumentException("유효한 멤버 ID가 필요합니다.");
        }

        if (quizId == null) {
            throw new IllegalArgumentException("유효한 퀴즈 ID가 필요합니다.");
        }

        if( isCorrect == null) {
            throw new IllegalArgumentException("정답 여부는 null이 될 수 없습니다.");
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
