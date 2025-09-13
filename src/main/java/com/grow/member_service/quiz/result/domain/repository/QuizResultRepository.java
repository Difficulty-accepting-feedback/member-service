package com.grow.member_service.quiz.result.domain.repository;

import java.util.List;
import java.util.Optional;

import com.grow.member_service.quiz.result.domain.model.QuizResult;

public interface QuizResultRepository {
    List<QuizResult> findByMemberId(Long memberId);
    void upsert(Long memberId, Long quizId, boolean isCorrect);
    Optional<QuizResult> findOne(Long memberId, Long quizId);
}