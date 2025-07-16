package com.grow.member_service.common.exception;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;

public class SubscriptionHistoryException extends ServiceException {

	public SubscriptionHistoryException(ErrorCode errorCode) {
		super(errorCode);
	}

	public SubscriptionHistoryException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}