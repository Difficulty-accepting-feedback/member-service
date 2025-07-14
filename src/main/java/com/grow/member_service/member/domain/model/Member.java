package com.grow.member_service.member.domain.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import com.grow.member_service.member.domain.exception.MemberDomainException;

import lombok.Getter;

/**
 * 순수 도메인 엔티티
 */
@Getter
public class Member {
    private final Long memberId;
    private MemberProfile memberProfile;
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
    /** 회원 탈퇴 여부 조회 */
    public boolean isWithdrawn() {
        return this.withdrawalAt != null;
    }

    /** 포인트 추가 메서드 */
    public void addPoint(int points) {
        if (points < 0) {
            throw MemberDomainException.negativePoints(points);
        }
        this.totalPoint += points;
    }

    /** 핸드폰 인증 완료 처리 */
    public void verifyPhone(String phoneNumber) {
        if (this.isPhoneVerified()) {
            throw MemberDomainException.alreadyPhoneVerified();
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw MemberDomainException.invalidPhoneNumber();
        }
        this.additionalInfo = this.additionalInfo.verifyPhone(phoneNumber);
    }

    /** 인증 여부 조회 편의 메서드 */
    public boolean isPhoneVerified() {
        return this.additionalInfo.isPhoneVerified();
    }

    /** 회원 탈퇴 처리 및 개인정보 마스킹 */
    public void markAsWithdrawn(UUID uuid) {
        String suffix = uuid + "_" + LocalDateTime.now();
        this.memberProfile = this.memberProfile.maskSensitiveInfo(this.memberId, suffix);
        this.additionalInfo = this.additionalInfo.eraseSensitiveInfo();
        this.withdrawalAt = LocalDateTime.now();
    }

    /** 회원 탈퇴 로그로 변환 */
    public MemberWithdrawalLog toWithdrawalLog() {
        return new MemberWithdrawalLog(
            this.getMemberId(),
            this.getMemberProfile().getEmail(),
            this.getMemberProfile().getNickname(),
            this.getMemberProfile().getPlatform(),
            this.getMemberProfile().getPlatformId(),
            this.additionalInfo.getPhoneNumber(),
            this.getWithdrawalAt()
        );
    }
}