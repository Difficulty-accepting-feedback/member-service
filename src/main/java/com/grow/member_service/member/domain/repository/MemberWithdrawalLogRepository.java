package com.grow.member_service.member.domain.repository;

import java.util.Optional;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberWithdrawalLog;

public interface MemberWithdrawalLogRepository {
	void saveFromMember(Member member);
	boolean existsByPlatformId(String platformId);
	Optional<MemberWithdrawalLog> findByPlatformId(String platformId);
}