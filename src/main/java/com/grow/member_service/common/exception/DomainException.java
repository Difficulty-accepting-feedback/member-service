package com.grow.member_service.common.exception;


public class DomainException extends RuntimeException {
	public DomainException(String message) {
		super(message);
	}
}