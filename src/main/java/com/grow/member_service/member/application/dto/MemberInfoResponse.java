package com.grow.member_service.member.application.dto;

import java.time.LocalDateTime;

import com.grow.member_service.member.domain.model.Member;

import lombok.Getter;

@Getter
public class MemberInfoResponse {
	private Long memberId;
	private String email;
	private String nickname;
	private String profileImage;
	private LocalDateTime joinedAt;
	private int totalPoint;
	private double score;

	// Member → DTO 변환 팩토리 메서드
	public static MemberInfoResponse from(Member m) {
		MemberInfoResponse dto = new MemberInfoResponse();
		dto.memberId     = m.getMemberId();
		dto.email        = m.getMemberProfile().getEmail();
		dto.nickname     = m.getMemberProfile().getNickname();
		dto.profileImage = m.getMemberProfile().getProfileImage();
		dto.joinedAt     = m.getCreateAt();
		dto.totalPoint   = m.getTotalPoint();
		dto.score        = m.getScore();
		return dto;
	}
}