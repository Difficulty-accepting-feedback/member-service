package com.grow.member_service.member.infra.persistence.mapper;

import org.springframework.stereotype.Component;

import com.grow.member_service.member.domain.model.MemberWithdrawalLog;
import com.grow.member_service.member.infra.persistence.entity.MemberWithdrawalLogJpaEntity;

@Component
public class MemberWithdrawalLogMapper {

	public MemberWithdrawalLogJpaEntity toEntity(MemberWithdrawalLog log) {
		return MemberWithdrawalLogJpaEntity.builder()
			.memberId(log.getMemberId())
			.email(log.getOriginalEmail())
			.nickname(log.getOriginalNickname())
			.platform(log.getPlatform())
			.platformId(log.getOriginalPlatformId())
			.phoneNumber(log.getOriginalPhoneNumber())
			.withdrawnAt(log.getWithdrawnAt())
			.build();
	}

	public MemberWithdrawalLog toDomain(MemberWithdrawalLogJpaEntity entity) {
		return new MemberWithdrawalLog(
			entity.getMemberId(),
			entity.getEmail(),
			entity.getNickname(),
			entity.getPlatform(),
			entity.getPlatformId(),
			entity.getPhoneNumber(),
			entity.getWithdrawnAt()
		);
	}
}