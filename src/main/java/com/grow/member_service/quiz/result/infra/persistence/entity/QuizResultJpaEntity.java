package com.grow.member_service.quiz.result.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "quizResult")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizResultJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quizResultId", nullable = false)
    private Long quizResultId;

    @Column(name = "memberId",
            nullable = false,
            updatable = false,
            unique = true
    )
    private Long memberId; // 멤버 ID

    @Column(name = "quizId", nullable = false, updatable = false)
    private Long quizId; // 퀴즈 (실제 문제) ID

    @Column(name = "isCorrect", nullable = false)
    private Boolean isCorrect; // 문제의 정답 여부

    @Builder
    public QuizResultJpaEntity(Long memberId,
                               Long quizId,
                               Boolean isCorrect
    ) {
        this.memberId = memberId;
        this.quizId = quizId;
        this.isCorrect = isCorrect;
    }
}
