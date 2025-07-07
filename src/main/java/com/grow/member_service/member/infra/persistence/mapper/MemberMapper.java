package com.grow.member_service.member.infra.persistence.mapper;

import org.springframework.stereotype.Component;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;

/**
 * 도메인 - 엔티티 변환 클래스
 */
@Component
public class MemberMapper {

    // 엔티티를 도메인으로 변환 (조회 용도)
    public Member toDomain(MemberJpaEntity entity) {
        MemberProfile memberProfile = new MemberProfile(
                entity.getEmail(),
                entity.getNickname(),
                entity.getProfileImage(),
                entity.getPlatform(),
                entity.getPlatformId()
        );

        MemberAdditionalInfo additionalInfo = new MemberAdditionalInfo(
                entity.getPhoneNumber(),
                entity.getAddress()
        );

        return new Member(
                entity.getMemberId(),
                memberProfile,
                additionalInfo,
                entity.getCreateAt(),
                entity.getTotalPoint(),
                entity.getScore()
        );
    }

    // 도메인을 엔티티로 변환
    public MemberJpaEntity toEntity(Member domain) {
        MemberProfile profile     = domain.getMemberProfile();
        MemberAdditionalInfo info = domain.getAdditionalInfo();

        return MemberJpaEntity.builder()
            .memberId(domain.getMemberId())              // 기존 ID 또는 null
            .email(profile.getEmail())
            .nickname(profile.getNickname())
            .profileImage(profile.getProfileImage())
            .platform(profile.getPlatform())
            .platformId(profile.getPlatformId())
            .phoneNumber(info.getPhoneNumber())
            .address(info.getAddress())
            .createAt(domain.getCreateAt())
            .withdrawalAt(domain.getWithdrawalAt())       // 탈퇴 일시(없으면 null)
            .totalPoint(domain.getTotalPoint())
            .score(domain.getScore())
            .build();
    }
}