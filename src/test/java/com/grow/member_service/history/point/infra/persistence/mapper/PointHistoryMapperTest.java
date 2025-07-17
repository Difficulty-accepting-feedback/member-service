package com.grow.member_service.history.point.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;

class PointHistoryMapperTest {

	private PointHistoryMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new PointHistoryMapper();
	}

	@Test
	@DisplayName("toDomain: JPA 엔티티 → 도메인 매핑")
	void toDomain() {
		// given
		Long id        = 123L;
		Long memberId  = 42L;
		Integer amount = 500;
		String content = "포인트 적립";
		LocalDateTime at = LocalDateTime.of(2025, 7, 17, 10, 0);

		// 1) 엔티티 빌더로 생성 (ID는 null)
		PointHistoryJpaEntity entity = PointHistoryJpaEntity.builder()
			.memberId(memberId)
			.amount(amount)
			.content(content)
			.addAt(at)
			.build();

		// 2) ReflectionTestUtils로 private 필드에 ID 주입
		ReflectionTestUtils.setField(entity, "pointHistoryId", id);

		// when
		PointHistory domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getPointHistoryId()).isEqualTo(id);
		assertThat(domain.getMemberId()).isEqualTo(memberId);
		assertThat(domain.getAmount()).isEqualTo(amount);
		assertThat(domain.getContent()).isEqualTo(content);
		assertThat(domain.getAddAt()).isEqualTo(at);
	}

	@Test
	@DisplayName("toEntity: 도메인 → JPA 엔티티 매핑")
	void toEntity() {
		// given
		Long id        = 999L;
		Long memberId  = 24L;
		Integer amount = -200;
		String content = "포인트 사용";
		LocalDateTime at = LocalDateTime.of(2025, 7, 1, 8, 30);

		// 도메인 모델(모든 필드 생성자)로 생성
		PointHistory domain = new PointHistory(id, memberId, amount, content, at);

		// when
		PointHistoryJpaEntity entity = mapper.toEntity(domain);

		// then
		// builder로 생성된 JPA 엔티티는 ID(null)만 보장
		assertThat(entity.getPointHistoryId()).isNull();
		assertThat(entity.getMemberId()).isEqualTo(memberId);
		assertThat(entity.getAmount()).isEqualTo(amount);
		assertThat(entity.getContent()).isEqualTo(content);
		assertThat(entity.getAddAt()).isEqualTo(at);
	}
}