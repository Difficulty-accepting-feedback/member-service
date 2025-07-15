package com.grow.member_service.review.application.dto;

import com.grow.member_service.member.domain.model.Member;

public class ReviewCandidateResponse {

	private final Long memberId;
	private final String nickname;
	private final String profileImage;

	public ReviewCandidateResponse(Long memberId, String nickname, String profileImage) {
		this.memberId = memberId;
		this.nickname = nickname;
		this.profileImage = profileImage;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getNickname() {
		return nickname;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public static ReviewCandidateResponse from(Member member) {
		return new ReviewCandidateResponse(
			member.getMemberId(),
			member.getMemberProfile().getNickname(),
			member.getMemberProfile().getProfileImage()
		);
	}
}