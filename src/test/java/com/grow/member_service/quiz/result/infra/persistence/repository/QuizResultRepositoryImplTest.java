package com.grow.member_service.quiz.result.infra.persistence.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.infra.persistence.mapper.QuizResultMapper;

@DataJpaTest
@Import(QuizResultMapper.class)
@DisplayName("QuizResultRepositoryImpl 테스트")
class QuizResultRepositoryImplTest {

	@Autowired
	private QuizResultJpaRepository jpaRepo;

	@Autowired
	private QuizResultMapper mapper;

	private QuizResultRepositoryImpl repository;

	@BeforeEach
	void init() {
		repository = new QuizResultRepositoryImpl(mapper, jpaRepo);
	}

	@Test
	@DisplayName("save & findByMemberId: 도메인 객체가 정상 영속화되어 조회되는지")
	void saveAndFindByMemberId_persistsAndRetrievesDomain() {
		// given
		QuizResult domain = new QuizResult(1L, 5L, true);

		// when
		QuizResult saved = repository.save(domain);
		List<QuizResult> found = repository.findByMemberId(1L);

		// then
		assertThat(found)
			.hasSize(1)
			.first()
			.extracting(QuizResult::getMemberId, QuizResult::getQuizId, QuizResult::getIsCorrect)
			.containsExactly(1L, 5L, true);
	}
}
