package com.grow.member_service.challenge.challenge.infra.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.challenge.challenge.infra.persistence.entity.ChallengeJpaEntity;

public interface ChallengeJpaRepository extends JpaRepository<ChallengeJpaEntity, Long> {
}