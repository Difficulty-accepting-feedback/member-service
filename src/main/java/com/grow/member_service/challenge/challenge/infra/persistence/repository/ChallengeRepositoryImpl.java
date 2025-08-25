package com.grow.member_service.challenge.challenge.infra.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.grow.member_service.challenge.challenge.domain.model.Challenge;
import com.grow.member_service.challenge.challenge.domain.repository.ChallengeRepository;
import com.grow.member_service.challenge.challenge.infra.persistence.mapper.ChallengeMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepository {

	private final ChallengeJpaRepository jpa;
	private final ChallengeMapper mapper;

	@Override public Optional<Challenge> findById(Long id) {
		return jpa.findById(id).map(mapper::toDomain);
	}
}