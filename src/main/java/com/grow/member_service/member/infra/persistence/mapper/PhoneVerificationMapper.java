package com.grow.member_service.member.infra.persistence.mapper;

import org.springframework.stereotype.Component;

import com.grow.member_service.member.domain.model.PhoneVerification;
import com.grow.member_service.member.infra.persistence.entity.PhoneVerificationJpaEntity;

@Component
public class PhoneVerificationMapper {

	public PhoneVerificationJpaEntity toEntity(PhoneVerification domain) {
		return PhoneVerificationJpaEntity.builder()
			.id(domain.getId())
			.memberId(domain.getMemberId())
			.phoneNumber(domain.getPhoneNumber())
			.code(domain.getCode())
			.createdAt(domain.getCreatedAt())
			.verified(domain.isVerified())
			.build();
	}

	public PhoneVerification toDomain(PhoneVerificationJpaEntity entity) {
		return new PhoneVerification(
			entity.getId(),
			entity.getMemberId(),
			entity.getPhoneNumber(),
			entity.getCode(),
			entity.getCreatedAt(),
			entity.isVerified()
		);
	}
}