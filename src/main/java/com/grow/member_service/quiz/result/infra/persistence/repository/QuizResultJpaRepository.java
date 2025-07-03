package com.grow.member_service.quiz.result.infra.persistence.repository;

import com.grow.member_service.quiz.result.infra.persistence.entity.QuizResultJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultJpaRepository extends JpaRepository<QuizResultJpaEntity, Long> {
    List<QuizResultJpaEntity> findByMemberId(Long memberId);
}
