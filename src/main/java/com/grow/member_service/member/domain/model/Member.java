package com.grow.member_service.member.domain.model;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import com.grow.member_service.member.domain.exception.MemberDomainException;
import com.grow.member_service.member.domain.service.MemberService;

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
	private boolean matchingEnabled; // 매칭 기능 활성화 여부

    public Member(MemberProfile memberProfile,
        MemberAdditionalInfo additionalInfo,
        Clock createAt
    ) {
        this.memberId = null; // 생성 시 null, DB 저장 후 자동으로 생성
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.totalPoint = 0;
        this.score = 36.5;
        this.matchingEnabled = true;

        if (createAt != null) {
            this.createAt = LocalDateTime.now(createAt);
        } else {
            this.createAt = LocalDateTime.now();
        }
    }

    public Member(Long memberId,
        MemberProfile memberProfile,
        MemberAdditionalInfo additionalInfo,
        LocalDateTime createAt,
        int totalPoint,
        double score,
        boolean matchingEnabled
    ) {
        this.memberId = memberId;
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.createAt = createAt;
        this.totalPoint = totalPoint;
        this.score = score;
        this.matchingEnabled = matchingEnabled;
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

    /** 프로필 점수(온도) 조정 메서드 */
    public void adjustScore(double scoreChange) {
        this.score += scoreChange;
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
        // 이미 탈퇴된 회원인지 검사
        if (this.withdrawalAt != null) {
            throw MemberDomainException.alreadyWithdrawn();
        }
        // 탈퇴 시각 설정
        this.withdrawalAt = LocalDateTime.now();
        String suffix = uuid + "_" + this.withdrawalAt;
        // 개인정보 마스킹
        this.memberProfile = this.memberProfile.maskSensitiveInfo(this.memberId, suffix);
        this.additionalInfo = this.additionalInfo.eraseSensitiveInfo();
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

    /**
     * 닉네임 변경
     */
    public void changeNickname(String newNickname, MemberService memberService) {
        String nickname = java.util.Objects.requireNonNull(newNickname, "변경할 닉네임은 null일 수 없습니다.");
        if (!memberService.isNicknameUnique(nickname)) {
            throw MemberDomainException.nicknameAlreadyExists(nickname);
        }
        this.memberProfile = this.memberProfile.withNickname(nickname);
    }

    /** 프로필 이미지 변경 */
    public void changeProfileImage(String newProfileImage) {
        this.memberProfile = this.memberProfile.withProfileImage(newProfileImage);
    }

    /** 주소 변경 */
    public void changeAddress(String newAddress) {
        String addr = java.util.Objects.requireNonNull(newAddress, "변경할 주소는 null일 수 없습니다.");
        this.additionalInfo = this.additionalInfo.withAddress(addr);
    }

    /** 매칭 기능 활성화 */
    public void enableMatching() {
        this.matchingEnabled = true;
    }

    /** 매칭 기능 비활성화 */
    public void disableMatching() {
        this.matchingEnabled = false;
    }

    /** 주소 정규화 메서드 */
    public boolean changeAddressIfDifferent(String nextRegionLabel) {
        if (this.isWithdrawn()) {
            throw MemberDomainException.alreadyWithdrawn();
        }
        String normalized = normalizeRegion(nextRegionLabel);
        String current = java.util.Optional.ofNullable(this.additionalInfo.getAddress()).orElse("");
        if (normalized.isEmpty() || normalized.equals(current)) {
            return false; // 변경 없음
        }
        this.additionalInfo = this.additionalInfo.withAddress(normalized);
        return true;
    }

    /** 주소 정규화 헬퍼 메서드 */
    private static String normalizeRegion(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("\\s+", " ").trim();
    }
}