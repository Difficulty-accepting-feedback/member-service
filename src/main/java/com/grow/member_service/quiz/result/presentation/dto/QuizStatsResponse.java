package com.grow.member_service.quiz.result.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizStatsResponse {
	private long correctCount; // 정답 개수
	private double successRate; // 성공률 (정답 개수 / 총 문제 개수 * 100)
}
