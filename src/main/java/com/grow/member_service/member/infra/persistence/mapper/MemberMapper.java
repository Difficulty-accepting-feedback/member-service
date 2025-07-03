package com.grow.member_service.member.infra.persistence.mapper;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;
import org.springframework.stereotype.Component;

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
        MemberAdditionalInfo additionalInfo = domain.getAdditionalInfo();
        MemberProfile memberProfile = domain.getMemberProfile();

        return MemberJpaEntity.builder()
                .email(memberProfile.getEmail())
                .nickname(memberProfile.getNickname())
                .profileImage(memberProfile.getProfileImage())
                .platform(memberProfile.getPlatform())
                .platformId(memberProfile.getPlatformId())
                .phoneNumber(additionalInfo.getPhoneNumber())
                .address(additionalInfo.getAddress())
                .createAt(domain.getCreateAt())
                .build();
    }
}
