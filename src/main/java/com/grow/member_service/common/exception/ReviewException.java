package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class ReviewException extends ServiceException {

	public ReviewException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ReviewException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}