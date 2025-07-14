package com.grow.member_service.member.infra.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.member.infra.persistence.entity.MemberWithdrawalLogJpaEntity;

public interface MemberWithdrawalLogJpaRepository extends JpaRepository<MemberWithdrawalLogJpaEntity, Long> {
	boolean existsByPlatformId(String platformId);
	Optional<MemberWithdrawalLogJpaEntity> findByPlatformId(String platformId);
}