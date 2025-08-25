package com.grow.member_service.history.point.domain.model.enums;


public enum PointActionType {
	DAILY_CHECK_IN,   // 매일 출석
	STREAK_3, STREAK_7, // 연속 출석 3일, 7일
	POST_CREATE,      // 게시글 작성
	ACHIEVEMENT,	  // 업적 달성
	ADMIN_ADJUST      // 운영자  +++ 다른 경우 추가 필요
}