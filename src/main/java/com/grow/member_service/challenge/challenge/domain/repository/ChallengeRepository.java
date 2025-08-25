package com.grow.member_service.challenge.challenge.domain.repository;

import java.util.Optional;

import com.grow.member_service.challenge.challenge.domain.model.Challenge;

public interface ChallengeRepository {
	Optional<Challenge> findById(Long id);
}