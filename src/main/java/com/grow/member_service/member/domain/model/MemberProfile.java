package com.grow.member_service.member.domain.model;

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
        this.email = validateEmail(email);
        this.nickname = validateNickname(nickname);
        this.profileImage = profileImage;
        this.platform = platform;
        this.platformId = platformId;
    }

    /**
     * 중복 확인 로직 추가 필요
     */
    private String validateEmail(String email) {
        return email;
    }

    /**
     * 중복 확인 로직 추가 필요
     */
    private String validateNickname(String nickname) {
        return nickname;
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