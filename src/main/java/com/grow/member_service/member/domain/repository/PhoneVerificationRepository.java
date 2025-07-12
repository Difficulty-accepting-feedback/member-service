package com.grow.member_service.member.domain.repository;

import java.util.Optional;

import com.grow.member_service.member.domain.model.PhoneVerification;

public interface PhoneVerificationRepository {
	PhoneVerification save(PhoneVerification request);
	Optional<PhoneVerification> findById(Long id);
	Optional<PhoneVerification> findByMemberId(Long memberId);
}