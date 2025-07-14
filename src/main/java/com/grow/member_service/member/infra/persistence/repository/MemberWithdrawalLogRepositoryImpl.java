package com.grow.member_service.member.infra.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberWithdrawalLog;
import com.grow.member_service.member.domain.repository.MemberWithdrawalLogRepository;
import com.grow.member_service.member.infra.persistence.mapper.MemberWithdrawalLogMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberWithdrawalLogRepositoryImpl implements MemberWithdrawalLogRepository {

	private final MemberWithdrawalLogJpaRepository jpaRepository;
	private final MemberWithdrawalLogMapper mapper;

	@Override
	public void saveFromMember(Member member) {
		MemberWithdrawalLog log = member.toWithdrawalLog();
		jpaRepository.save(mapper.toEntity(log));
	}

	@Override
	public boolean existsByPlatformId(String platformId) {
		return jpaRepository.existsByPlatformId(platformId);
	}

	@Override
	public Optional<MemberWithdrawalLog> findByPlatformId(String platformId) {
		return jpaRepository.findByPlatformId(platformId)
			.map(mapper::toDomain);
	}
}