package com.grow.member_service.quiz.result.infra.persistence.mapper;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.infra.persistence.entity.QuizResultJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class QuizResultMapper {

    // 엔티티를 도메인으로 변경
    public QuizResult toDomain(QuizResultJpaEntity entity) {
        return new QuizResult(
                entity.getMemberId(),
                entity.getQuizId(),
                entity.getIsCorrect()
        );
    }

    // 도메인을 엔티티로 변경
    public QuizResultJpaEntity toEntity(QuizResult result) {
        return QuizResultJpaEntity.builder()
                .memberId(result.getMemberId())
                .quizId(result.getQuizId())
                .isCorrect(result.getIsCorrect())
                .build();
    }

}
