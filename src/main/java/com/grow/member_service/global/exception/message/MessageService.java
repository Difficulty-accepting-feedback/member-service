package com.grow.member_service.global.exception.message;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
	private final MessageSource messageSource;

	public String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.KOREA);
	}
}
