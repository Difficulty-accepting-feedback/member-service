package com.grow.member_service.member.application.service;

import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.presentation.dto.MemberUpdateRequest;

public interface MemberApplicationService {
	MemberInfoResponse getMyInfo(Long memberId);
	void withdraw(Long memberId);
	void updateMember(Long memberId, MemberUpdateRequest req);
}