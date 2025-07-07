package com.grow.member_service.member.infra.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class KakaoUserInfo {
	@JsonProperty("id")
	private String id;

	@JsonProperty("kakao_account")
	private Account kakaoAccount;

	@Getter
	public static class Account {
		@JsonProperty("email")
		private String email;
		@JsonProperty("profile")
		private Profile profile;
	}

	@Getter
	public static class Profile {
		@JsonProperty("nickname")
		private String nickname;
		@JsonProperty("profile_image_url")
		private String profileImage;
	}

	public String getEmail() {
		return kakaoAccount.getEmail();
	}
	public String getNickname() {
		return kakaoAccount.getProfile().getNickname();
	}
	public String getProfileImage() {
		return kakaoAccount.getProfile().getProfileImage();
	}
}