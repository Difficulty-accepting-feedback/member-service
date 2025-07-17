package com.grow.member_service.member.domain.model;

import java.util.Objects;

import com.grow.member_service.member.domain.model.enums.Platform;

import lombok.Getter;

/**
 * Value Object 생성, 소셜 가입 시 넘어오는 정보를 기반
 */

@Getter
public class MemberProfile {

    private final String email;
    private String nickname;
    private final String profileImage;
    private final Platform platform;
    private final String platformId;

    public MemberProfile(String email,
                         String nickname,
                         String profileImage,
                         Platform platform,
                         String platformId
    ) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.platform = platform;
        this.platformId = platformId;
    }

    public MemberProfile withNickname(String newNickname) {
        return new MemberProfile(
            this.email,
            Objects.requireNonNull(newNickname, "새 닉네임은 null일 수 없습니다."),
            this.profileImage,
            this.platform,
            this.platformId
        );
    }

    public MemberProfile withProfileImage(String newProfileImage) {
        return new MemberProfile(
            this.email,
            this.nickname,
            newProfileImage,
            this.platform,
            this.platformId
        );
    }

    /** 회원 탈퇴 시 민감 정보 마스킹 처리 */
    public MemberProfile maskSensitiveInfo(Long memberId, String suffix) {
        return new MemberProfile(
            "withdrawn_" + memberId + "_" + suffix + "@masked.com",
            "탈퇴회원_" + suffix,
            null,
            Platform.NONE,
            "withdrawn_" + memberId + "_" + suffix
        );
    }
}