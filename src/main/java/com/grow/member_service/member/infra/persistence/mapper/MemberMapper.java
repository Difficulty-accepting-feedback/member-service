package com.grow.member_service.member.infra.persistence.mapper;

import org.springframework.stereotype.Component;
import com.grow.member_service.member.domain.model.*;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;

@Component
public class MemberMapper {

    // 엔티티 -> 도메인 (그대로 유지)
    public Member toDomain(MemberJpaEntity entity) {
        MemberProfile profile = new MemberProfile(
            entity.getEmail(), entity.getNickname(), entity.getProfileImage(),
            entity.getPlatform(), entity.getPlatformId()
        );
        MemberAdditionalInfo info = new MemberAdditionalInfo(
            entity.getPhoneNumber(), entity.getAddress(), entity.isPhoneVerified()
        );
        return new Member(
            entity.getMemberId(), profile, info, entity.getCreateAt(),
            entity.getTotalPoint(), entity.getScore(), entity.isMatchingEnabled(),
            entity.getLastAttendanceDay(), entity.getAttendanceStreak(), entity.getAttendanceBestStreak()
        );
    }

    /**
     * 도메인 -> 신규 엔티티 변환
     * - 신규 회원 가입 시 사용
     * - id/version은 null로 설정하여 새 엔티티로 처리
     */
    public MemberJpaEntity toNewEntity(Member d) {
        MemberProfile p = d.getMemberProfile();
        MemberAdditionalInfo a = d.getAdditionalInfo();
        return MemberJpaEntity.builder()
            .email(p.getEmail())
            .nickname(p.getNickname())
            .profileImage(p.getProfileImage())
            .platform(p.getPlatform())
            .platformId(p.getPlatformId())
            .phoneNumber(a.getPhoneNumber())
            .phoneVerified(a.isPhoneVerified())
            .address(a.getAddress())
            .createAt(d.getCreateAt())
            .withdrawalAt(d.getWithdrawalAt())
            .totalPoint(d.getTotalPoint())
            .score(d.getScore())
            .matchingEnabled(d.isMatchingEnabled())
            .lastAttendanceDay(d.getLastAttendanceDay())
            .attendanceStreak(d.getAttendanceStreak())
            .attendanceBestStreak(d.getAttendanceBestStreak())
            .build();
    }

    /**
     * 업데이트용: 기존 엔티티의 id/version을 그대로 이식하여
     * 새 detached 엔티티를 빌더로 조립 → merge(save) 경로로 처리.
     */
    public MemberJpaEntity toEntityForUpdate(Member d, MemberJpaEntity current) {
        MemberProfile p = d.getMemberProfile();
        MemberAdditionalInfo a = d.getAdditionalInfo();
        return MemberJpaEntity.builder()
            .memberId(current.getMemberId())      // ★ 기존 id 유지
            .version(current.getVersion())        // ★ 기존 version 유지 (NULL 금지)
            .email(p.getEmail())
            .nickname(p.getNickname())
            .profileImage(p.getProfileImage())
            .platform(p.getPlatform())
            .platformId(p.getPlatformId())
            .phoneNumber(a.getPhoneNumber())
            .phoneVerified(a.isPhoneVerified())
            .address(a.getAddress())
            .createAt(d.getCreateAt())
            .withdrawalAt(d.getWithdrawalAt())
            .totalPoint(d.getTotalPoint())
            .score(d.getScore())
            .matchingEnabled(d.isMatchingEnabled())
            .lastAttendanceDay(d.getLastAttendanceDay())
            .attendanceStreak(d.getAttendanceStreak())
            .attendanceBestStreak(d.getAttendanceBestStreak())
            .build();
    }
}