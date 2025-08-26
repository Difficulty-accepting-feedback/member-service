package com.grow.member_service.accomplished.infra.persistence.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.grow.member_service.achievement.accomplished.infra.persistence.entity.AccomplishedJpaEntity;
import com.grow.member_service.achievement.accomplished.infra.persistence.repository.AccomplishedJpaRepository;

@DataJpaTest
class AccomplishedJpaEntityTest {

	@Autowired
	private AccomplishedJpaRepository repo;

	@Test
	@DisplayName("엔티티 저장 후 조회가 가능해야 한다")
	void saveAndFindById() {
		// given
		AccomplishedJpaEntity entity = AccomplishedJpaEntity.builder()
			.memberId(1L)
			.challengeId(100L)
			.accomplishedAt(LocalDateTime.of(2025, 7, 17, 10, 0))
			.build();

		// when
		AccomplishedJpaEntity saved = repo.save(entity);
		Optional<AccomplishedJpaEntity> found = repo.findById(saved.getAccomplishedId());

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getMemberId()).isEqualTo(1L);
		assertThat(found.get().getChallengeId()).isEqualTo(100L);
		assertThat(found.get().getAccomplishedAt())
			.isEqualTo(LocalDateTime.of(2025, 7, 17, 10, 0));
	}

	@Test
	@DisplayName("memberId+challengeId 유니크 제약 위반 시 예외가 발생한다")
	void uniqueConstraintViolation() {
		// given
		AccomplishedJpaEntity a1 = AccomplishedJpaEntity.builder()
			.memberId(2L)
			.challengeId(200L)
			.accomplishedAt(LocalDateTime.now())
			.build();
		repo.saveAndFlush(a1);

		AccomplishedJpaEntity a2 = AccomplishedJpaEntity.builder()
			.memberId(2L)
			.challengeId(200L)
			.accomplishedAt(LocalDateTime.now())
			.build();

		// then
		assertThatThrownBy(() -> {
			repo.saveAndFlush(a2);
		}).isInstanceOf(DataIntegrityViolationException.class);
	}
}