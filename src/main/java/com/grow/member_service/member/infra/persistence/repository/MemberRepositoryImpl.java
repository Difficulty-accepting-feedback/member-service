package com.grow.member_service.member.infra.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
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
    @Transactional
    public Member save(final Member domain) {
        // 신규: id == null -> persist
        if (domain.getMemberId() == null) {
            MemberJpaEntity e = memberMapper.toNewEntity(domain);
            MemberJpaEntity saved = memberJpaRepository.save(e); // persist
            return memberMapper.toDomain(saved);
        }

        // 업데이트: id != null → 현재 버전 로딩 후, 버전 이식한 detached를 merge
        MemberJpaEntity current = memberJpaRepository.findById(domain.getMemberId())
            .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 버전과 비교하여 낙관적 락 충돌 여부 확인
        MemberJpaEntity toMerge = memberMapper.toEntityForUpdate(domain, current);
        MemberJpaEntity merged = memberJpaRepository.save(toMerge); // merge
        return memberMapper.toDomain(merged);
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return memberJpaRepository.findById(memberId)
            .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByPlatformId(String platformId, Platform platform) {
        return memberJpaRepository.findByPlatformIdAndPlatform(platformId, platform)
            .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByNickname(String nickname) {
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

    @Override
    public Optional<Member> findActiveByNicknameIgnoreCase(String nickname) {
        return memberJpaRepository
            .findByNicknameIgnoreCaseAndWithdrawalAtIsNull(nickname)
            .map(memberMapper::toDomain);
    }
}