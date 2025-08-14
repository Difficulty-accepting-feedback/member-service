package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class LocationException extends ServiceException {
	public LocationException(ErrorCode errorCode) {
		super(errorCode);
	}
	public LocationException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}