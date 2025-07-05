package com.grow.member_service.common;


public class DomainException extends RuntimeException {
	public DomainException(String message) {
		super(message);
	}
}