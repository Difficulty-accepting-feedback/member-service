package com.grow.member_service.member.domain.model;

import lombok.Getter;

/**
 * Value Object 생성
 */

@Getter
public class MemberAdditionalInfo {

    private final String phoneNumber;
    private final String address;
    private final boolean phoneVerified;

    // 최초 생성 시엔 verified=false
    public MemberAdditionalInfo(String phoneNumber, String address) {
        this(phoneNumber, address, false);
    }

    public MemberAdditionalInfo(String phoneNumber,
        String address,
        boolean phoneVerified) {
        this.phoneNumber     = validatePhoneNumber(phoneNumber);
        this.address         = address;
        this.phoneVerified   = phoneVerified;
    }

    /** 전화번호 검증 후, 인증 완료 상태로 반환 */
    public MemberAdditionalInfo verifyPhone(String phoneNumber) {
        return new MemberAdditionalInfo(phoneNumber, this.address, true);
    }

    /** 전화번호 검증 메서드 (소셜 로그인 시 핸드폰 번호 초기값 못 받아오기 때문) */
    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }
        return phoneNumber;
    }
}