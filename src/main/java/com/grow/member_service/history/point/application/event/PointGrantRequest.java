package com.grow.member_service.history.point.application.event;

import java.time.LocalDateTime;


public record PointGrantRequest(
	Long memberId,
	Integer amount,          // +적립(음수는 차감 정책에 따라 허용)
	String actionType,       // "DAILY_CHECK_IN", "POST_CREATE", "PHONE_VERIFIED" 등 (enum name string)
	String sourceType,       // "ATTENDANCE", "BOARD", "SYSTEM" 등
	String sourceId,         // postId, yyyy-MM-dd, "phone-verify" 등
	String content,          // 원장 사유
	String dedupKey,         // 멱등 키 (UNIQUE)
	LocalDateTime occurredAt // 없으면 now
) {}