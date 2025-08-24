package com.grow.member_service.member.domain.model;

import java.time.Clock;
import java.time.LocalDate;
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

    // 출석 체크 관련 필드
    private LocalDate lastAttendanceDay;
    private int attendanceStreak;
    private int attendanceBestStreak;

    public Member(MemberProfile memberProfile,
        MemberAdditionalInfo additionalInfo,
        Clock createAt) {
        this.memberId = null;
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.totalPoint = 0;
        this.score = 36.5;
        this.matchingEnabled = true;
        this.createAt = (createAt != null) ? LocalDateTime.now(createAt) : LocalDateTime.now();
        this.lastAttendanceDay = null;
        this.attendanceStreak = 0;
        this.attendanceBestStreak = 0;
    }

    public Member(Long memberId,
        MemberProfile memberProfile,
        MemberAdditionalInfo additionalInfo,
        LocalDateTime createAt,
        int totalPoint,
        double score,
        boolean matchingEnabled) {
        this.memberId = memberId;
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.createAt = createAt;
        this.totalPoint = totalPoint;
        this.score = score;
        this.matchingEnabled = matchingEnabled;
        this.lastAttendanceDay = null;
        this.attendanceStreak = 0;
        this.attendanceBestStreak = 0;
    }

    // [선호] 출석 스냅샷이 포함된 풀 생성자(매퍼에서 사용)
    public Member(Long memberId,
        MemberProfile memberProfile,
        MemberAdditionalInfo additionalInfo,
        LocalDateTime createAt,
        int totalPoint,
        double score,
        boolean matchingEnabled,
        LocalDate lastAttendanceDay,
        int attendanceStreak,
        int attendanceBestStreak) {
        this.memberId = memberId;
        this.memberProfile = memberProfile;
        this.additionalInfo = additionalInfo;
        this.createAt = createAt;
        this.totalPoint = totalPoint;
        this.score = score;
        this.matchingEnabled = matchingEnabled;
        this.lastAttendanceDay = lastAttendanceDay;
        this.attendanceStreak = attendanceStreak;
        this.attendanceBestStreak = attendanceBestStreak;
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

    /** 포인트 사용 메서드 */
    public void usePoint(int points) {
        if (points <= 0) throw MemberDomainException.negativePoints(points);
        if (this.totalPoint - points < 0) throw MemberDomainException.notEnoughPoints(points, this.totalPoint);
        this.totalPoint -= points;
    }

    // 포인트 차감 메서드
    public void deductPoint(int points) {
        if (points < 0) {
            throw MemberDomainException.negativePoints(points);
        }
        if (this.totalPoint < points) {
            throw MemberDomainException.notEnoughPoints(points, this.totalPoint);
        }
        this.totalPoint -= points;
    }

    /** 출석 체크 메서드 */
    public boolean markAttendance(LocalDate today) {
        if (this.lastAttendanceDay != null && this.lastAttendanceDay.isEqual(today)) {
            return false; // 오늘 이미 처리됨 -> 출석 체크 중복 방지
        }
        if (this.lastAttendanceDay != null && this.lastAttendanceDay.plusDays(1).isEqual(today)) {
            this.attendanceStreak = this.attendanceStreak + 1;
        } else {
            this.attendanceStreak = 1;
        }
        if (this.attendanceStreak > this.attendanceBestStreak) {
            this.attendanceBestStreak = this.attendanceStreak;
        }
        this.lastAttendanceDay = today;
        return true;
    }
}