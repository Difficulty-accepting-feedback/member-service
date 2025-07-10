package com.grow.member_service.member.domain.service;

public interface SmsClient {
	void send(String to, String text);
}