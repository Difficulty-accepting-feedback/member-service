package com.grow.member_service.quiz.result.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(
    name = "quiz_result",
    uniqueConstraints = @UniqueConstraint(name = "uk_member_quiz", columnNames = {"member_id","quiz_id"}),
    indexes = @Index(name = "idx_qr_member", columnList = "member_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuizResultJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_result_id", nullable = false)
    private Long quizResultId;

    @Column(name = "member_id", nullable = false, updatable = false)
    private Long memberId;

    @Column(name = "quiz_id", nullable = false, updatable = false)
    private Long quizId; // 퀴즈 (실제 문제) ID

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect; // 문제의 정답 여부
}