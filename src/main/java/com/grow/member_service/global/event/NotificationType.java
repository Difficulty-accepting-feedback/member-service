package com.grow.member_service.global.event;

import lombok.Getter;

@Getter
public enum NotificationType {
	// 멤버
	ADDR_REMINDER("SERVICE_NOTICE", "주소 정보 미입력", "더 정확한 매칭을 위해 주소 정보를 등록해 주세요."),
	PHONE_REMINDER("SERVICE_NOTICE", "휴대폰 인증 미완료", "보안 강화를 위해 휴대폰 번호 인증을 완료해 주세요."),
	PHONE_VERIFIED("SERVICE_NOTICE", "인증 완료", "휴대폰 번호 인증이 완료되었습니다."),

	// 포인트
	POINT_GRANT("POINT", "포인트 적립 안내", "포인트가 적립되었습니다."),
	POINT_SPEND("POINT", "포인트 사용 안내", "포인트가 차감되었습니다.");

	private final String notificationType;
	private final String title;
	private final String content;

	NotificationType(String notificationType, String title, String content) {
		this.notificationType = notificationType;
		this.title = title;
		this.content = content;
	}
}