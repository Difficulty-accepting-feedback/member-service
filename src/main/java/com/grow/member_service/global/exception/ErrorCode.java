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
	NULL_CORRECTNESS(HttpStatus.BAD_REQUEST, "400-2", "quizResult.null.correctness"),

	// OAuth 관련
	OAUTH_UNSUPPORTED_PLATFORM(HttpStatus.BAD_REQUEST, "400-3", "oauth.unsupported.platform"),
	OAUTH_MISSING_PLATFORM_ID(HttpStatus.BAD_REQUEST, "400-4", "oauth.missing.platform.id"),
	OAUTH_PARSING_FAILED(HttpStatus.BAD_REQUEST, "400-5", "oauth.parsing.failed"),
	OAUTH_INVALID_STRUCTURE(HttpStatus.BAD_REQUEST, "400-6", "oauth.invalid.structure"),
	OAUTH_MEMBER_ID_PARSE_ERROR(HttpStatus.BAD_REQUEST, "400-7", "oauth.member.id.parse.error"),
	OAUTH_INVALID_ATTRIBUTE(HttpStatus.BAD_REQUEST, "400-8", "oauth.invalid.attribute");
	private final HttpStatus status;
	private final String code;
	private final String messageCode;
}