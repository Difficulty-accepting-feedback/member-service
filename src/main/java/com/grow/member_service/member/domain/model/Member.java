package com.grow.member_service.member.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 순수 도메인 엔티티
 */
@Getter
public class Member {
    private final Long memberId;
    private final MemberProfile memberProfile;
    private final MemberAdditionalInfo additionalInfo;
    private final LocalDateTime createAt;
    private LocalDateTime withdrawalAt;
    private int totalPoint;
    private int score;

    public Member(MemberProfile memberProfile,
                  MemberAdditionalInfo additionalInfo) {
        this.memberId = null; // 생성 시 null, DB 저장 후 자동으로 생성
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.createAt = null; // 생성 시 null, DB 저장 후 자동으로 생성
        this.totalPoint = 0;
        this.score = 0;
    }

    // 비즈니스 로직 메서드
    public void withdraw() {
        if (this.withdrawalAt != null) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }
        this.withdrawalAt = LocalDateTime.now();
    }

    public void addPoint(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("포인트는 0 이상이어야 합니다.");
        }
        this.totalPoint += points;
    }
}
