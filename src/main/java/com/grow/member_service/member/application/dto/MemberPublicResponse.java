package com.grow.member_service.member.application.dto;

import com.grow.member_service.member.domain.model.Member;

public class MemberPublicResponse {
	private Long memberId;
	private String nickname;
	private String profileImage;

	public MemberPublicResponse() {}

	public MemberPublicResponse(Long memberId, String nickname, String profileImage) {
		this.memberId = memberId;
		this.nickname = nickname;
		this.profileImage = profileImage;
	}

	public Long getMemberId() { return memberId; }
	public String getNickname() { return nickname; }
	public String getProfileImage() { return profileImage; }

	public static MemberPublicResponse of(Member m) {
		return new MemberPublicResponse(
			m.getMemberId(),
			m.getMemberProfile().getNickname(),
			m.getMemberProfile().getProfileImage()
		);
	}
}