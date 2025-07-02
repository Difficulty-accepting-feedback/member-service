package com.grow.member_service.quiz.result.infra.persistence.repository;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.repository.QuizResultRepository;
import com.grow.member_service.quiz.result.infra.persistence.mapper.QuizResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuizResultRepositoryImpl implements QuizResultRepository {

    private final QuizResultMapper mapper;
    private final QuizResultJpaRepository jpaRepository;

    @Override
    public Long save(QuizResult quizResult) {
        return 0L;
    }

    @Override
    public List<QuizResult> findByMemberId(Long memberId) {
        return List.of();
    }

    @Override
    public List<QuizResult> findByQuizId(Long quizId) {
        return List.of();
    }
}
