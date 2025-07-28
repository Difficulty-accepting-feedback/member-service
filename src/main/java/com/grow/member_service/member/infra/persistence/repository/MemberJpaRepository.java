package com.grow.member_service.member.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.grow.member_service.member.infra.dto.MemberScoreProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
	Optional<MemberJpaEntity> findByPlatformIdAndPlatform(String platformId, Platform platform);
	List<MemberJpaEntity> findAllByWithdrawalAtBefore(LocalDateTime threshold);
	Optional<MemberJpaEntity> findByNickname(String nickname);
	List<MemberJpaEntity> findByMemberIdNot(Long memberId);
	List<MemberScoreProjection> findAllBy(); // memberId와 score를 가진 dto 리스트 반환
}