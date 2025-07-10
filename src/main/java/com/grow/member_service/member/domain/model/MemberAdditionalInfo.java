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

    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null; // 소셜로그인 테스트 위해 임시로 null 허용
        }
        return phoneNumber;
    }
}