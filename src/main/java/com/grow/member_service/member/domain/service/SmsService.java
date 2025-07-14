package com.grow.member_service.member.domain.service;

public interface SmsService {
	void send(String to, String text);
}