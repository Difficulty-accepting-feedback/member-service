package com.grow.member_service.member.domain.model;

import lombok.Getter;

/**
 * Value Object 생성
 */

@Getter
public class MemberAdditionalInfo {

    private final String phoneNumber;
    private final String address;

    public MemberAdditionalInfo(String phoneNumber, String address) {
        this.phoneNumber = validatePhoneNumber(phoneNumber);
        this.address = address;
    }

    // 중복 확인 로직 필요
    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        return phoneNumber;
    }
}
