package com.grow.member_service.quiz.result.domain.repository;

import com.grow.member_service.quiz.result.domain.model.QuizResult;

import java.util.List;

public interface QuizResultRepository {
    QuizResult save(QuizResult quizResult);
    List<QuizResult> findByMemberId(Long memberId);
    List<QuizResult> findByQuizId(Long quizId);
}
