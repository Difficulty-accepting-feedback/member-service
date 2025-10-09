package com.grow.member_service.member.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.infra.dto.MemberScoreProjection;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
	@Query("select m from MemberJpaEntity m " +
		"where m.platformId = :platformId and m.platform = :platform and m.withdrawalAt is null")
	Optional<MemberJpaEntity> findByPlatformIdAndPlatform(String platformId, Platform platform);

	List<MemberJpaEntity> findAllByWithdrawalAtBefore(LocalDateTime threshold);

	@Query("select m from MemberJpaEntity m " +
		"where m.nickname = :nickname and m.withdrawalAt is null")
	Optional<MemberJpaEntity> findByNickname(String nickname);

	@Query("select m from MemberJpaEntity m " +
		"where m.memberId <> :memberId and m.withdrawalAt is null")
	List<MemberJpaEntity> findByMemberIdNot(Long memberId);

	@Query("select m.memberId as memberId, m.score as score " +
		"from MemberJpaEntity m where m.withdrawalAt is null")
	List<MemberScoreProjection> findAllBy(); // memberId와 score를 가진 dto 리스트 반환

	@Query("select m from MemberJpaEntity m " +
		"where m.memberId in :ids and m.withdrawalAt is null")
	List<MemberJpaEntity> findAllByMemberIdIn(List<Long> ids);

	Optional<MemberJpaEntity> findByNicknameIgnoreCaseAndWithdrawalAtIsNull(String nickname); // 대소문자 구분 없이, 탈퇴하지 않은 회원 검색
}