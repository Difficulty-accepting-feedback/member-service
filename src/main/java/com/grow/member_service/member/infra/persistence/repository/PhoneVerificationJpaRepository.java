package com.grow.member_service.member.infra.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.member.infra.persistence.entity.PhoneVerificationJpaEntity;

public interface PhoneVerificationJpaRepository
	extends JpaRepository<PhoneVerificationJpaEntity, Long> {
	Optional<PhoneVerificationJpaEntity> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);
}