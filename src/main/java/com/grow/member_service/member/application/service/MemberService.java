package com.grow.member_service.member.application.service;

import com.grow.member_service.member.application.dto.MemberInfoResponse;

public interface MemberService {
	MemberInfoResponse getMyInfo(Long memberId);
}