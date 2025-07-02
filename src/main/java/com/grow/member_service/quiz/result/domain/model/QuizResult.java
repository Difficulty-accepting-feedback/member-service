package com.grow.member_service.quiz.result.domain.model;

import lombok.Getter;

@Getter
public class QuizResult {

    private Long quizResultId;
    private Long memberId; // 멤버 ID
    private Long quizId; // 퀴즈 (실제 문제) ID
    private Boolean isCorrect; // 문제의 정답 여부

    public QuizResult(Long memberId,
                      Long quizId,
                      Boolean isCorrect
    ) {
        this.quizResultId = null;
        this.memberId = memberId;
        this.quizId = quizId;
        this.isCorrect = isCorrect;
    }
}
