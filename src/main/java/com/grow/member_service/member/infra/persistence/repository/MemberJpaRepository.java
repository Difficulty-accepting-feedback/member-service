package com.grow.member_service.member.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findByEmail(String email);
	Optional<MemberJpaEntity> findByPlatformIdAndPlatform(String platformId, Platform platform);
	List<MemberJpaEntity> findAllByWithdrawalAtBefore(LocalDateTime threshold);
}