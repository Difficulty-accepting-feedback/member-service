package com.grow.member_service.admin.application;

public interface AdminCommandService {
	Long grant(Long memberId);
	void revoke(Long memberId);
}