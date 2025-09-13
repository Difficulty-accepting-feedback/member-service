package com.grow.member_service.quiz.result.infra.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grow.member_service.quiz.result.infra.persistence.entity.QuizResultJpaEntity;

public interface QuizResultJpaRepository extends JpaRepository<QuizResultJpaEntity, Long> {
    Optional<QuizResultJpaEntity> findByMemberIdAndQuizId(Long memberId, Long quizId);

    List<QuizResultJpaEntity> findByMemberId(Long memberId);

    /**
     * 퀴즈 결과를 삽입하거나 업데이트합니다.
     * @param memberId 멤버 ID
     * @param quizId 퀴즈 ID
     * @param isCorrect 정답 여부
     */
    @Modifying
    @Query(value = """
    INSERT INTO quiz_result (member_id, quiz_id, is_correct)
    VALUES (:memberId, :quizId, :isCorrect)
    ON DUPLICATE KEY UPDATE is_correct = VALUES(is_correct)
  """, nativeQuery = true)
    void upsert(@Param("memberId") Long memberId,
        @Param("quizId") Long quizId,
        @Param("isCorrect") boolean isCorrect);
    List<QuizResultJpaEntity> findByMemberIdAndIsCorrect(Long memberId, Boolean isCorrect);
}