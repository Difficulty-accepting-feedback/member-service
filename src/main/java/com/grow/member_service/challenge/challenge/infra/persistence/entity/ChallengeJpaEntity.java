package com.grow.member_service.challenge.challenge.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name="challenge")
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PROTECTED)


public class ChallengeJpaEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long challengeId;

	@Column(nullable=false)
	private String name;

	@Column(nullable=false)
	private String description;

	@Column(nullable=false)
	private int point;
}