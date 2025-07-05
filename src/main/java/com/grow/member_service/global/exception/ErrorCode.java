package com.grow.member_service.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500-0", "error.server.internal"),
	INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "400-0", "invalid.member.id"),

	// 퀴즈 결과 도메인
	INVALID_QUIZ_ID(HttpStatus.BAD_REQUEST, "400-1", "quizResult.result.invalid.id"),
	NULL_CORRECTNESS(HttpStatus.BAD_REQUEST, "400-2", "quizResult.null.correctness");

	private final HttpStatus status;
	private final String code;
	private final String messageCode;
}