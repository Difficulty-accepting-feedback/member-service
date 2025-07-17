package com.grow.member_service.review.application.dto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;

class ReviewCandidateResponseTest {

	@Test
	@DisplayName("from(): Member → ReviewCandidateResponse 매핑")
	void from_member_mapsAllFields() {
		// given
		Long memberId = 123L;
		MemberProfile profile = new MemberProfile(
			"user@example.com",
			"nick123",
			"http://example.com/avatar.png",
			Platform.KAKAO,
			"ext-id-xyz"
		);
		MemberAdditionalInfo additionalInfo = new MemberAdditionalInfo("010-0000-0000", "Seoul");
		LocalDateTime createAt = LocalDateTime.ofEpochSecond(1626000000L, 0, ZoneOffset.UTC);
		Member member = new Member(
			memberId,
			profile,
			additionalInfo,
			createAt,
			0,
			36.5,
			true
		);

		// when
		ReviewCandidateResponse dto = ReviewCandidateResponse.from(member);

		// then
		assertThat(dto.getMemberId()).isEqualTo(memberId);
		assertThat(dto.getNickname()).isEqualTo("nick123");
		assertThat(dto.getProfileImage()).isEqualTo("http://example.com/avatar.png");
	}
}