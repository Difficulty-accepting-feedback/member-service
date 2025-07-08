package com.grow.member_service.member.application.service;

import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.auth.application.dto.TokenResponse;

public interface MemberService {
	TokenResponse loginWithKakao(String code);
	MemberInfoResponse getMyInfo(Long memberId);
}