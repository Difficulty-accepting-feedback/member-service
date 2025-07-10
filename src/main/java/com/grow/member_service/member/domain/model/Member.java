package com.grow.member_service.member.domain.model;

import java.time.Clock;
import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 순수 도메인 엔티티
 */
@Getter
public class Member {
    private final Long memberId;
    private final MemberProfile memberProfile;
    private MemberAdditionalInfo additionalInfo;
    private final LocalDateTime createAt;
    private LocalDateTime withdrawalAt;
    private int totalPoint;
    private double score;

    public Member(MemberProfile memberProfile,
                  MemberAdditionalInfo additionalInfo,
                  Clock createAt
    ) {
        this.memberId = null; // 생성 시 null, DB 저장 후 자동으로 생성
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.totalPoint = 0;
        this.score = 36.5;

        if (createAt != null) {
            this.createAt = LocalDateTime.now(createAt);
        } else  {
            this.createAt = LocalDateTime.now();
        }
    }

    public Member(Long memberId,
                  MemberProfile memberProfile,
                  MemberAdditionalInfo additionalInfo,
                  LocalDateTime createAt,
                  int totalPoint,
                  double score
    ) {
        this.memberId = memberId;
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.createAt = createAt;
        this.totalPoint = totalPoint;
        this.score = score;
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

    /** 핸드폰 인증 완료 처리 */
    public void verifyPhone(String phoneNumber) {
        this.additionalInfo = this.additionalInfo.verifyPhone(phoneNumber);
    }

    /** 인증 여부 조회 편의 메서드 */
    public boolean isPhoneVerified() {
        return this.additionalInfo.isPhoneVerified();
    }
}