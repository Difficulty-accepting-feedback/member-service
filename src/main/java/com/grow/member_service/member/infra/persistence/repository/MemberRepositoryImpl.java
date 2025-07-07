package com.grow.member_service.member.infra.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;
import com.grow.member_service.member.infra.persistence.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberMapper memberMapper;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = memberMapper.toEntity(member);
        MemberJpaEntity saved = memberJpaRepository.save(entity);
        return memberMapper.toDomain(saved);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id)
            .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
            .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByPlatformId(String platformId, Platform platform) {
        return memberJpaRepository.findByPlatformIdAndPlatform(platformId, platform)
            .map(memberMapper::toDomain);
    }

    @Override
    public List<Member> findAll() {
        return memberJpaRepository.findAll()
            .stream()
            .map(memberMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Member member) {
        memberJpaRepository.delete(memberMapper.toEntity(member));
    }
}