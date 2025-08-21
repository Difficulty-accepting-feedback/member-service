package com.grow.member_service.member.application.service;

public interface ScoreUpdateService {

	/** 단일 멤버 점수를 Redis에 저장/업데이트 */
	void saveMemberScoreToRedis(Long memberId, Double score);

	/** 전체 멤버 점수를 조회하여 Redis에 동기화(배치/스케줄) */
	void updateAllMemberScores();
}