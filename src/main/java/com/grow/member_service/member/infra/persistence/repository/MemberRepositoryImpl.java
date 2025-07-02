package com.grow.member_service.member.infra.persistence.repository;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;
import com.grow.member_service.member.infra.persistence.mapper.MemberMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberMapper memberMapper;

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = memberMapper.toEntity(member);
        return memberMapper.toDomain(memberJpaRepository.save(entity));
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
