package com.grow.member_service.member.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;
import com.grow.member_service.member.infra.persistence.entity.QMemberJpaEntity;
import com.grow.member_service.member.infra.persistence.mapper.MemberMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberMapper memberMapper;
    private final MemberJpaRepository memberJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final QMemberJpaEntity qMemberJpaEntity = QMemberJpaEntity.memberJpaEntity;


    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = memberMapper.toEntity(member);
        MemberJpaEntity saved = memberJpaRepository.save(entity);
        return memberMapper.toDomain(saved);
    }

    @Override
    public Optional<Member> findById(Long id) {
        MemberJpaEntity entity = jpaQueryFactory
            .selectFrom(qMemberJpaEntity)
            .where(qMemberJpaEntity.memberId.eq(id)
                .and(qMemberJpaEntity.withdrawalAt.isNull()))
            .fetchOne();
        return Optional.ofNullable(entity)
            .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        MemberJpaEntity entity = jpaQueryFactory
            .selectFrom(qMemberJpaEntity)
            .where(qMemberJpaEntity.email.eq(email)
                .and(qMemberJpaEntity.withdrawalAt.isNull()))
            .fetchOne();
        return Optional.ofNullable(entity)
            .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByPlatformId(String platformId, Platform platform) {
        MemberJpaEntity entity = jpaQueryFactory
            .selectFrom(qMemberJpaEntity)
            .where(qMemberJpaEntity.platformId.eq(platformId)
                .and(qMemberJpaEntity.platform.eq(platform))
                .and(qMemberJpaEntity.withdrawalAt.isNull()))
            .fetchOne();
        return Optional.ofNullable(entity)
            .map(memberMapper::toDomain);
    }

    @Override
    public List<Member> findAll() {
        return jpaQueryFactory
            .selectFrom(qMemberJpaEntity)
            .where(qMemberJpaEntity.withdrawalAt.isNull())
            .fetch()
            .stream()
            .map(memberMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Member member) {
        new JPAUpdateClause(entityManager, qMemberJpaEntity)
            .where(qMemberJpaEntity.memberId.eq(member.getMemberId())
            .and(qMemberJpaEntity.withdrawalAt.isNull()))
            .set(qMemberJpaEntity.withdrawalAt, LocalDateTime.now())
            .execute();
    }
}