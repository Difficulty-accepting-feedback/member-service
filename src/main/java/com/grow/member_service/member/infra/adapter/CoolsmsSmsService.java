package com.grow.member_service.member.infra.adapter;

import org.springframework.stereotype.Component;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;

import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.domain.service.SmsService;
import com.grow.member_service.member.infra.config.SmsProperties;

import jakarta.annotation.PostConstruct;

@Component
public class CoolsmsSmsService implements SmsService {
	private DefaultMessageService messageService;
	private final SmsProperties props;

	public CoolsmsSmsService(SmsProperties props) {
		this.props = props;
	}

	@PostConstruct
	private void init() {
		this.messageService = NurigoApp
				.INSTANCE
				.initialize(
			props.getApiKey(),
			props.getApiSecret(),
			"https://api.coolsms.co.kr"
		);
	}
	@Override
	public void send(String to, String text) {
		Message msg = new Message();
		msg.setFrom(props.getFrom());
		msg.setTo(to);
		msg.setText(text);
		try {
			messageService.send(msg);
		} catch (Exception e) {
			throw new MemberException(ErrorCode.SMS_SEND_FAILED, e);
		}
	}
}