package com.grow.member_service.member.application.service;

import java.time.LocalDateTime;

import com.grow.member_service.member.application.dto.AttendanceResult;

public interface MemberAttendanceApplicationService {
	AttendanceResult checkInAndReward(Long memberId, LocalDateTime occurredAt);
}