package com.grow.member_service.member.infra.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.member.domain.model.PhoneVerification;
import com.grow.member_service.member.domain.repository.PhoneVerificationRepository;
import com.grow.member_service.member.infra.persistence.entity.PhoneVerificationJpaEntity;
import com.grow.member_service.member.infra.persistence.mapper.PhoneVerificationMapper;

@Component
@Transactional
public class PhoneVerificationRepositoryImpl implements PhoneVerificationRepository {

	private final PhoneVerificationJpaRepository jpaRepo;
	private final PhoneVerificationMapper mapper;

	public PhoneVerificationRepositoryImpl(
		PhoneVerificationJpaRepository jpaRepo,
		PhoneVerificationMapper mapper
	) {
		this.jpaRepo = jpaRepo;
		this.mapper = mapper;
	}

	@Override
	public PhoneVerification save(PhoneVerification request) {
		PhoneVerificationJpaEntity entity = mapper.toEntity(request);
		PhoneVerificationJpaEntity saved = jpaRepo.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	public Optional<PhoneVerification> findById(Long id) {
		return jpaRepo.findById(id)
			.map(mapper::toDomain);
	}

	@Override
	public Optional<PhoneVerification> findByMemberId(Long memberId) {
		return jpaRepo
			.findFirstByMemberIdOrderByCreatedAtDesc(memberId)
			.map(entity -> new PhoneVerification(
				entity.getId(),
				entity.getMemberId(),
				entity.getPhoneNumber(),
				entity.getCode(),
				entity.getCreatedAt(),
				entity.isVerified()
			));
	}
}