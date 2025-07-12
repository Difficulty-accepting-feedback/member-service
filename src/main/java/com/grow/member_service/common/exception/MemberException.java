package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class MemberException extends ServiceException {
	public MemberException(ErrorCode errorCode) {
		super(errorCode);
	}
	public MemberException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}