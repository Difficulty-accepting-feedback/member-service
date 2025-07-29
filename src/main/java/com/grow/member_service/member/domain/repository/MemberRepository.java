package com.grow.member_service.member.domain.repository;

import java.util.List;
import java.util.Optional;

import com.grow.member_service.member.domain.model.MemberScoreInfo;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.enums.Platform;

/**
 * 도메인 리포지토리 인터페이스
 */
public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long memberId);
    Optional<Member> findByPlatformId(String platformId, Platform platform);
    Optional<Object> findByNickname(String nickname);
    List<Member> findAllExcept(Long exceptMemberId);
    List<MemberScoreInfo> findAllScore();
}