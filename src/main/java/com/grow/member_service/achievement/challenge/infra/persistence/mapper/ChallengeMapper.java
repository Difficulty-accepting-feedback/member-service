package com.grow.member_service.achievement.challenge.infra.persistence.mapper;

import org.springframework.stereotype.Component;

import com.grow.member_service.achievement.challenge.domain.model.Challenge;
import com.grow.member_service.achievement.challenge.infra.persistence.entity.ChallengeJpaEntity;

@Component
public class ChallengeMapper {
	public Challenge toDomain(ChallengeJpaEntity e) {
		return new Challenge(
			e.getChallengeId(),
			e.getName(),
			e.getDescription(),
			e.getPoint()
		);
	}
}