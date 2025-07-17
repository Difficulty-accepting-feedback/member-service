package com.grow.member_service.member.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.member.domain.model.PhoneVerification;
import com.grow.member_service.member.infra.persistence.entity.PhoneVerificationJpaEntity;

class PhoneVerificationMapperTest {

	private PhoneVerificationMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new PhoneVerificationMapper();
	}

	@Test
	@DisplayName("toEntity(): 도메인 → JPA 엔티티 매핑 확인")
	void toEntity_shouldMapDomainToEntity() {
		// given
		Long id = 100L;
		Long memberId = 200L;
		String phoneNumber = "010-1111-2222";
		String code = "654321";
		Instant createdAt = Instant.parse("2025-07-17T10:30:00Z");
		boolean verified = true;

		PhoneVerification domain = new PhoneVerification(
			id,
			memberId,
			phoneNumber,
			code,
			createdAt,
			verified
		);

		// when
		PhoneVerificationJpaEntity entity = mapper.toEntity(domain);

		// then
		assertThat(entity.getId()).isEqualTo(id);
		assertThat(entity.getMemberId()).isEqualTo(memberId);
		assertThat(entity.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(entity.getCode()).isEqualTo(code);
		assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
		assertThat(entity.isVerified()).isEqualTo(verified);
	}

	@Test
	@DisplayName("toDomain(): JPA 엔티티 → 도메인 매핑 확인")
	void toDomain_shouldMapEntityToDomain() {
		// given
		Long id = 300L;
		Long memberId = 400L;
		String phoneNumber = "010-3333-4444";
		String code = "123456";
		Instant createdAt = Instant.parse("2025-08-01T09:45:00Z");
		boolean verified = false;

		PhoneVerificationJpaEntity entity = PhoneVerificationJpaEntity.builder()
			.id(id)
			.memberId(memberId)
			.phoneNumber(phoneNumber)
			.code(code)
			.createdAt(createdAt)
			.verified(verified)
			.build();

		// when
		PhoneVerification domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getId()).isEqualTo(id);
		assertThat(domain.getMemberId()).isEqualTo(memberId);
		assertThat(domain.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(domain.getCode()).isEqualTo(code);
		assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
		assertThat(domain.isVerified()).isEqualTo(verified);
	}
}