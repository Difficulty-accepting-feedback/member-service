package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class OAuthException extends ServiceException {
	public OAuthException(ErrorCode errorCode) {
		super(errorCode);
	}
	public OAuthException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}