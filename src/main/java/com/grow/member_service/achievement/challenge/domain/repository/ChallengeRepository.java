package com.grow.member_service.achievement.challenge.domain.repository;

import java.util.Optional;

import com.grow.member_service.achievement.challenge.domain.model.Challenge;

public interface ChallengeRepository {
	Optional<Challenge> findById(Long id);
}