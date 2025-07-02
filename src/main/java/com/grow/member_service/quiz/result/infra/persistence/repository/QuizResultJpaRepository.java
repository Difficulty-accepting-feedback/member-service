package com.grow.member_service.quiz.result.infra.persistence.repository;

import com.grow.member_service.quiz.result.infra.persistence.entity.QuizResultJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResultJpaRepository extends JpaRepository<QuizResultJpaEntity, Long> {
}
