package com.grow.member_service.member.domain.model;

import lombok.Getter;

/**
 * Value Object 생성
 */

@Getter
public class MemberAdditionalInfo {

    private String phoneNumber;
    private String address;

    public MemberAdditionalInfo(String phoneNumber,
                                String address
    ) {
        this.phoneNumber = validatePhoneNumber(phoneNumber);
        this.address = address;
    }

    /**
     * 중복 확인 로직 추가 필요
     */
    private String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null; // 소셜로그인 테스트 위해 임시로 null 허용
        }
        return phoneNumber;
    }
}