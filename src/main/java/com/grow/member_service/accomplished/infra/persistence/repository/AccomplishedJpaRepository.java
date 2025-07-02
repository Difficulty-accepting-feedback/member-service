package com.grow.member_service.accomplished.infra.persistence.repository;

import com.grow.member_service.accomplished.infra.persistence.entity.AccomplishedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccomplishedJpaRepository extends JpaRepository<AccomplishedJpaEntity, Long> {
    Optional<AccomplishedJpaEntity> findByMemberId(Long memberId);
}
