package com.grow.member_service.accomplished.infra.persistence.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.accomplished.domain.model.Accomplished;
import com.grow.member_service.accomplished.infra.persistence.entity.AccomplishedJpaEntity;

class AccomplishedMapperTest {

	private AccomplishedMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new AccomplishedMapper();
	}

	@Test
	@DisplayName("toDomain: JPA 엔티티 → 도메인 매핑 (ID 포함, 순수 리플렉션)")
	void toDomain_withReflection() throws Exception {
		Long id        = 123L;
		Long memberId  = 42L;
		Long challenge = 99L;
		LocalDateTime at = LocalDateTime.now();

		AccomplishedJpaEntity entity = AccomplishedJpaEntity.builder()
			.memberId(memberId)
			.challengeId(challenge)
			.accomplishedAt(at)
			.build();

		Field idField = AccomplishedJpaEntity.class.getDeclaredField("accomplishedId");
		idField.setAccessible(true);
		idField.set(entity, id);

		Accomplished domain = mapper.toDomain(entity);

		assertThat(domain.getAccomplishedId()).isEqualTo(id);
	}

	@Test
	@DisplayName("toEntity: 도메인 → JPA 엔티티 매핑")
	void toEntity() {
		// given
		Long memberId  = 7L;
		Long challenge = 88L;
		LocalDateTime at = LocalDateTime.now();
		Accomplished domain = new Accomplished(memberId, challenge, at);

		// when
		AccomplishedJpaEntity entity = mapper.toEntity(domain);

		// then
		assertThat(entity.getAccomplishedId()).isNull(); // builder 로는 ID 주입하지 않음
		assertThat(entity.getMemberId()).isEqualTo(memberId);
		assertThat(entity.getChallengeId()).isEqualTo(challenge);
		assertThat(entity.getAccomplishedAt()).isEqualTo(at);
	}
}