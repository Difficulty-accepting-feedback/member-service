package com.grow.member_service.member.application.event;

import java.time.LocalDateTime;

public record LoginSucceededEvent(Long memberId, LocalDateTime occurredAt) {

}