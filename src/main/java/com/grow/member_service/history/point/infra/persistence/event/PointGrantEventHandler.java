package com.grow.member_service.history.point.infra.persistence.event;

import static org.springframework.transaction.event.TransactionPhase.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.grow.member_service.history.point.application.event.PointGrantRequest;
import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PointGrantEventHandler {

	private final PointCommandService commandService;

	/**
	 * 포인트 지급 이벤트를 처리합니다.
	 * @param e 포인트 지급 이벤트 객체
	 */
	@TransactionalEventListener(phase = AFTER_COMMIT)
	public void on(PointGrantRequest e) {
		LocalDateTime when = (e.occurredAt() != null ? e.occurredAt() : LocalDateTime.now());

		commandService.grant(
			e.memberId(),
			e.amount().intValue(),
			PointActionType.valueOf(e.actionType()),
			SourceType.valueOf(e.sourceType()),
			e.sourceId(),
			e.content(),
			e.dedupKey(),
			when
		);
	}
}