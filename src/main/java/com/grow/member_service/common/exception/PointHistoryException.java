package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class PointHistoryException extends ServiceException {

	public PointHistoryException(ErrorCode errorCode) {
		super(errorCode);
	}

	public PointHistoryException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}