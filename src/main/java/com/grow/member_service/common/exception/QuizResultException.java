package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class QuizResultException extends ServiceException {
	public QuizResultException(ErrorCode code) {
		super(code);
	}

	public QuizResultException(ErrorCode code, Throwable cause) {
		super(code, cause);
	}
}