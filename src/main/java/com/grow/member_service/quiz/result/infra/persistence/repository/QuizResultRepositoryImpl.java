package com.grow.member_service.quiz.result.infra.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.repository.QuizResultRepository;
import com.grow.member_service.quiz.result.infra.persistence.mapper.QuizResultMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QuizResultRepositoryImpl implements QuizResultRepository {

    private final QuizResultMapper mapper;
    private final QuizResultJpaRepository jpaRepository;

    /**
     * 퀴즈 결과를 삽입하거나 업데이트합니다.
     * @param memberId 멤버 ID
     * @param quizId 퀴즈 ID
     * @param isCorrect 정답 여부
     */
    @Override
    @Transactional
    public void upsert(Long memberId, Long quizId, boolean isCorrect) {
        jpaRepository.upsert(memberId, quizId, isCorrect);
    }

    /**
     * 특정 멤버의 특정 퀴즈 결과를 조회합니다.
     * @param memberId 멤버 ID
     * @param quizId 퀴즈 ID
     * @return 퀴즈 결과 (없을 경우 빈 Optional)
     */
    @Override
    public Optional<QuizResult> findOne(Long memberId, Long quizId) {
        return jpaRepository.findByMemberIdAndQuizId(memberId, quizId).map(mapper::toDomain);
    }

    /**
     * 특정 멤버의 모든 퀴즈 결과를 조회합니다.
     * @param memberId 멤버 ID
     * @return 퀴즈 결과 리스트 (없을 경우 빈 리스트)
     */
    @Override
    public List<QuizResult> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId).stream().map(mapper::toDomain).toList();
    }
}