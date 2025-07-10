package com.grow.member_service.member.infra.external.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;

import com.grow.member_service.member.domain.service.SmsClient;

@Component
public class CoolsmsSmsClient implements SmsClient {
	private final DefaultMessageService messageService;
	private final String from;

	public CoolsmsSmsClient(
		@Value("${sms.api-key}") String apiKey,
		@Value("${sms.api-secret}") String apiSecret,
		@Value("${sms.from}") String from
	) {
		// Solapi(Java SDK v4) 초기화 :contentReference[oaicite:1]{index=1}
		this.messageService = NurigoApp.INSTANCE.initialize(
			apiKey, apiSecret, "https://api.solapi.com"
		);
		this.from = from;
	}

	@Override
	public void send(String to, String text) {
		Message msg = new Message();
		msg.setFrom(from);
		msg.setTo(to);
		msg.setText(text);
		try {
			messageService.send(msg);
		} catch (Exception e) {
			throw new RuntimeException("SMS 전송 실패", e);
		}
	}
}