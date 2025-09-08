package com.grow.member_service.history.point.infra.kafka.dlt;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.grow.member_service.global.slack.SlackErrorSendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointCommandDltConsumer {

	private final SlackErrorSendService slackErrorSendService;

	@KafkaListener(topics = "point.grant.requested.dlt", groupId = "point-service-dlt")
	public void consumeGrantDlt(String message) {
		log.info("[POINT DLT][GRANT] 포인트 지급 커맨드 실패 수신: {}", message == null ? "" : message.trim());

		slackErrorSendService.sendError(
			"포인트 지급 커맨드 처리 실패",
			"카테고리: [POINT → GRANT]\n"
				+ "상세: 포인트 지급 요청 처리에 실패하셨습니다.\n"
				+ "영향: 사용자 포인트 적립 누락 가능",
			message
		);

		log.info("[POINT DLT][GRANT] 실패 이벤트 처리 완료");
	}

	@KafkaListener(topics = "point.spend.requested.dlt", groupId = "point-service-dlt")
	public void consumeSpendDlt(String message) {
		log.info("[POINT DLT][SPEND] 포인트 차감 커맨드 실패 수신: {}", message == null ? "" : message.trim());

		slackErrorSendService.sendError(
			"포인트 차감 커맨드 처리 실패",
			"카테고리: [POINT → SPEND]\n"
				+ "상세: 포인트 차감 요청 처리에 실패하였습니다.\n"
				+ "영향: 사용자 포인트 잔액 불일치 가능",
			message
		);

		log.info("[POINT DLT][SPEND] 실패 이벤트 처리 완료");
	}
}