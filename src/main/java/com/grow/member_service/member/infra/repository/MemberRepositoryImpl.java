package com.grow.member_service.member.infra.repository;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.infra.entity.MemberEntity;
import com.grow.member_service.member.infra.mapper.MemberMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;
    private final MemberMapper memberMapper;

    @Override
    public Member save(Member member) {
        MemberEntity entity = memberMapper.toEntity(member);
        MemberEntity savedEntity = jpaMemberRepository.save(entity);
        return memberMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jpaMemberRepository.findById(id)
                .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return jpaMemberRepository.findByEmail(email)
                .map(memberMapper::toDomain);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll()
                .stream()
                .map(memberMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Member member) {
        MemberEntity entity = memberMapper.toEntity(member);
        jpaMemberRepository.delete(entity);
    }
}
