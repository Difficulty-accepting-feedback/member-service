package com.grow.member_service.history.point.infra.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.grow.member_service.global.util.JsonUtils;
import com.grow.member_service.history.point.application.event.PointGrantRequest;
import com.grow.member_service.history.point.application.event.PointSpendRequest;
import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointCommandConsumer {

	private final PointCommandService point;

	@KafkaListener(
		topics = "point.grant.requested",
		groupId = "point-service",
		concurrency = "3"
	)
	@RetryableTopic(attempts = "5",
		backoff = @Backoff(delay = 1000, multiplier = 2),
		dltTopicSuffix = ".dlt",
		autoCreateTopics = "true"
	)
	public void onGrant(ConsumerRecord<String, String> rec) throws Exception {
		PointGrantRequest e = JsonUtils.fromJson(rec.value(), PointGrantRequest.class);
		point.grant(
			e.memberId(),
			e.amount(),
			PointActionType.valueOf(e.actionType()),
			SourceType.valueOf(e.sourceType()),
			e.sourceId(),
			e.content(),
			e.dedupKey(),
			e.occurredAt()
		);
		log.info("[POINT][GRANT][RECV] topic={} part={} off={} key={} memberId={}",
			rec.topic(), rec.partition(), rec.offset(), rec.key(), e.memberId());
	}

	@KafkaListener(
		topics = "point.spend.requested",
		groupId = "point-service",
		concurrency = "3"
	)
	@RetryableTopic(attempts = "5",
		backoff = @Backoff(delay = 1000, multiplier = 2),
		dltTopicSuffix = ".dlt",
		autoCreateTopics = "true"
	)
	public void onSpend(ConsumerRecord<String, String> rec) throws Exception {
		PointSpendRequest e = JsonUtils.fromJson(rec.value(), PointSpendRequest.class);
		point.spend(
			e.memberId(),
			e.amount(),
			PointActionType.valueOf(e.actionType()),
			SourceType.valueOf(e.sourceType()),
			e.sourceId(),
			e.content(),
			e.dedupKey(),
			e.occurredAt()
		);
		log.info("[POINT][SPEND][RECV] topic={} part={} off={} key={} memberId={}",
			rec.topic(), rec.partition(), rec.offset(), rec.key(), e.memberId());
	}
}