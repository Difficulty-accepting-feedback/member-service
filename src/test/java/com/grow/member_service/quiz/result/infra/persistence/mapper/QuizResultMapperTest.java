package com.grow.member_service.quiz.result.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.infra.persistence.entity.QuizResultJpaEntity;

@DisplayName("GROW - QuizResultMapper 단위 테스트 (DDD 매핑 검증)")
class QuizResultMapperTest {

	QuizResultMapper mapper = new QuizResultMapper();

	@Test
	@DisplayName("toDomain(): JPA 엔티티를 도메인 모델로 변환 - 필드 일치, ID는 null일 수 있음")
	void toDomain_should_map_fields_from_entity() {
		// given: 빌더로 만든 엔티티(quizResultId는 DB 생성이므로 null 상태)
		QuizResultJpaEntity entity = QuizResultJpaEntity.builder()
			.memberId(1L)
			.quizId(2L)
			.isCorrect(true)
			.build();

		// when
		QuizResult domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getQuizResultId()).isNull(); // 빌더가 id를 채우지 않으므로 null이 정상
		assertThat(domain.getMemberId()).isEqualTo(1L);
		assertThat(domain.getQuizId()).isEqualTo(2L);
		assertThat(domain.getIsCorrect()).isTrue();
	}

	@Test
	@DisplayName("toEntity(): 도메인 모델을 JPA 엔티티로 변환 - ID는 매핑하지 않음(INSERT 시 DB가 생성)")
	void toEntity_should_map_fields_from_domain_and_ignore_id() {
		// given: 도메인(서비스 계층에서 이미 식별자가 있을 수 있으나 엔티티에는 매핑하지 않음)
		QuizResult domain = new QuizResult(
			99L,   // quizResultId (toEntity에서는 사용하지 않음)
			10L,   // memberId
			20L,   // quizId
			false  // isCorrect
		);

		// when
		QuizResultJpaEntity entity = mapper.toEntity(domain);

		// then
		assertThat(entity.getQuizResultId()).isNull();  // 엔티티 ID는 매핑 안 함 → DB 생성
		assertThat(entity.getMemberId()).isEqualTo(10L);
		assertThat(entity.getQuizId()).isEqualTo(20L);
		assertThat(entity.getIsCorrect()).isFalse();
	}
}