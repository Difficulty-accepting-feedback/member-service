package com.grow.member_service.member.domain.model;

import java.util.Objects;

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

    /** 회원 탈퇴 시 민감 정보 마스킹 처리 */
    public MemberAdditionalInfo eraseSensitiveInfo() {
        return new MemberAdditionalInfo(null, this.address, false);
    }

    public MemberAdditionalInfo withAddress(String newAddress) {
        return new MemberAdditionalInfo(
            this.phoneNumber,
            Objects.requireNonNull(newAddress, "새 주소는 null일 수 없습니다."),
            this.phoneVerified
        );
    }

    /** 주소가 비어있는지(누락) 여부 */
    public boolean isAddressMissing() {
        return this.address == null || this.address.isBlank();
    }

    /** 주소가 존재하는지(채워짐) 여부 */
    public boolean hasAddress() {
        return !isAddressMissing();
    }
}