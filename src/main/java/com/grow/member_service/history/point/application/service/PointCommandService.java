package com.grow.member_service.history.point.application.service;

import java.time.LocalDateTime;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

public interface PointCommandService {

	/**
	 * 포인트를 지급합니다.
	 * @param memberId 회원 ID
	 * @param amount 지급할 포인트 양 (양수는 적립, 음수는 차감 정책에 따라 허용)
	 * @param actionType 포인트 액션 타입
	 * @param sourceType 포인트 출처 타입 (예: ATTENDANCE, BOARD 등)
	 * @param sourceId 포인트 출처 ID (예: postId, 날짜 등)
	 * @param content 포인트 지급 사유 (예: "출석 체크", "게시글 작성" 등)
	 * @param dedupKey 멱등 키 (중복 지급 방지용, UNIQUE 제약 조건 적용)
	 * @param occurredAt 포인트 지급 발생 시각 (null이면 현재 시각 사용)
	 * @return 지급된 포인트 기록 객체
	 */
	PointHistory grant(Long memberId,
		int amount,
		PointActionType actionType,
		SourceType sourceType,
		String sourceId,
		String content,
		String dedupKey,
		LocalDateTime occurredAt);

	/**
	 * 포인트를 차감합니다.
	 * @param memberId 회원 ID
	 * @param amount 차감할 포인트 양
	 * @param actionType 포인트 액션 타입
	 * @param sourceType 포인트 출처 타입 (예: ATTENDANCE, BOARD 등)
	 * @param sourceId 포인트 출처 ID (예: postId, 날짜 등)
	 * @param content 포인트 차감 사유 (예: "게시글 삭제", "환불" 등)
	 * @param dedupKey 멱등 키 (중복 차감 방지용, UNIQUE 제약 조건 적용)
	 * @param occurredAt 포인트 차감 발생 시각 (null이면 현재 시각 사용)
	 * @return 차감된 포인트 기록 객체
	 */
	PointHistory spend(Long memberId,
		int amount,
		PointActionType actionType,
		SourceType sourceType,
		String sourceId,
		String content,
		String dedupKey,
		LocalDateTime occurredAt);
}