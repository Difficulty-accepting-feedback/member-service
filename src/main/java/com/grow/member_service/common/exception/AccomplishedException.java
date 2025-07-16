package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class AccomplishedException extends ServiceException {

	public AccomplishedException(ErrorCode errorCode) {
		super(errorCode);
	}

	public AccomplishedException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}