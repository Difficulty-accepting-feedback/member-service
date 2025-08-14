package com.grow.member_service.member.infra.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberScoreInfo;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.infra.dto.MemberScoreProjection;
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
    public Optional<Member> findByPlatformId(String platformId, Platform platform) {
        return memberJpaRepository.findByPlatformIdAndPlatform(platformId, platform)
            .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Object> findByNickname(String nickname) {
        return memberJpaRepository.findByNickname(nickname)
            .map(memberMapper::toDomain);
    }

    @Override
    public List<Member> findAllExcept(Long exceptMemberId) {
        return memberJpaRepository.findByMemberIdNot(exceptMemberId)
            .stream()
            .map(memberMapper::toDomain)
            .toList();
    }

    /**
     * 모든 멤버의 점수를 조회하여 MemberScoreDto 리스트로 반환합니다.
     *
     * <p>이 메서드는 데이터베이스에서 MemberScoreProjection을 조회한 후,
     * 이를 MemberScoreDto로 변환하여 반환합니다. Projection을 사용해 필요한 필드만 효율적으로 가져옵니다.</p>
     *
     * @return MemberScoreDto 리스트 (각 DTO는 memberId와 score를 포함)
     */
    @Override
    public List<MemberScoreInfo> findAllScore() {
        List<MemberScoreProjection> projections = memberJpaRepository.findAllBy();  // Projection 조회
        return projections.stream()
                .map(p -> new MemberScoreInfo(
                        p.getMemberId(),
                        p.getScore()
                ))// // Projection -> DTO 변환
                .toList();
    }

    @Override
    public List<Member> findAllByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<MemberJpaEntity> list = memberJpaRepository.findAllByMemberIdIn(ids);
        return list.stream().map(memberMapper::toDomain).collect(Collectors.toList());
    }
}