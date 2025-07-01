package com.grow.member_service.member.domain.repository;

import com.grow.member_service.member.domain.model.Member;

import java.util.List;
import java.util.Optional;

/**
 * 도메인 리포지토리 인터페이스
 */
public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    List<Member> findAll();
    void delete(Member member);
}
