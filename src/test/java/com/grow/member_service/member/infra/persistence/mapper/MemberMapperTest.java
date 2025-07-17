package com.grow.member_service.member.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.infra.persistence.entity.MemberJpaEntity;

class MemberMapperTest {

	private MemberMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new MemberMapper();
	}

	@Test
	@DisplayName("toDomain(): 엔티티의 모든 필드를 도메인으로 매핑한다")
	void toDomain_shouldMapEntityToDomain() {
		// given
		Long id = 10L;
		String email = "test@example.com";
		String nickname = "testNick";
		String profileImage = "http://img";
		Platform platform = Platform.KAKAO;
		String platformId = "extId";
		String phoneNumber = "010-1234-5678";
		boolean phoneVerified = true;
		String address = "Seoul";
		LocalDateTime createAt = LocalDateTime.of(2025, 7, 17, 12, 0);
		LocalDateTime withdrawalAt = LocalDateTime.of(2025, 7, 18, 13, 30);
		int totalPoint = 100;
		double score = 37.5;
		boolean matchingEnabled = true;

		MemberJpaEntity entity = MemberJpaEntity.builder()
			.memberId(id)
			.email(email)
			.nickname(nickname)
			.profileImage(profileImage)
			.platform(platform)
			.platformId(platformId)
			.phoneNumber(phoneNumber)
			.phoneVerified(phoneVerified)
			.address(address)
			.createAt(createAt)
			.withdrawalAt(withdrawalAt)
			.totalPoint(totalPoint)
			.score(score)
			.matchingEnabled(matchingEnabled)
			.build();

		// when
		Member domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getMemberId()).isEqualTo(id);
		assertThat(domain.getMemberProfile().getEmail()).isEqualTo(email);
		assertThat(domain.getMemberProfile().getNickname()).isEqualTo(nickname);
		assertThat(domain.getMemberProfile().getProfileImage()).isEqualTo(profileImage);
		assertThat(domain.getMemberProfile().getPlatform()).isEqualTo(platform);
		assertThat(domain.getMemberProfile().getPlatformId()).isEqualTo(platformId);
		assertThat(domain.getAdditionalInfo().getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(domain.getAdditionalInfo().getAddress()).isEqualTo(address);
		assertThat(domain.getAdditionalInfo().isPhoneVerified()).isEqualTo(phoneVerified);
		assertThat(domain.getCreateAt()).isEqualTo(createAt);
		// withdrawalAt은 도메인으로 매핑되지 않아 null이어야 한다
		assertThat(domain.getWithdrawalAt()).isNull();
		assertThat(domain.getTotalPoint()).isEqualTo(totalPoint);
		assertThat(domain.getScore()).isEqualTo(score);
		assertThat(domain.isMatchingEnabled()).isTrue();
	}

	@Test
	@DisplayName("toEntity(): 도메인의 모든 필드를 엔티티로 매핑한다 (withdrawalAt null)")
	void toEntity_shouldMapDomainToEntity() {
		// given
		Long id = 20L;
		String email = "user@example.com";
		String nickname = "nick";
		String profileImage = "img";
		Platform platform = Platform.GOOGLE;
		String platformId = "oid123";
		String phoneNumber = "010-0000-0000";
		String address = "Busan";
		LocalDateTime createAt = LocalDateTime.of(2025, 7, 10, 10, 0);
		int totalPoint = 50;
		double score = 39.5;
		boolean matchingEnabled = true;

		MemberProfile profile = new MemberProfile(email, nickname, profileImage, platform, platformId);
		MemberAdditionalInfo info = new MemberAdditionalInfo(phoneNumber, address);
		Member domain = new Member(id, profile, info, createAt, totalPoint, score, true);

		// when
		MemberJpaEntity entity = mapper.toEntity(domain);

		// then
		assertThat(entity.getMemberId()).isEqualTo(id);
		assertThat(entity.getEmail()).isEqualTo(email);
		assertThat(entity.getNickname()).isEqualTo(nickname);
		assertThat(entity.getProfileImage()).isEqualTo(profileImage);
		assertThat(entity.getPlatform()).isEqualTo(platform);
		assertThat(entity.getPlatformId()).isEqualTo(platformId);
		assertThat(entity.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(entity.isPhoneVerified()).isFalse();
		assertThat(entity.getAddress()).isEqualTo(address);
		assertThat(entity.getCreateAt()).isEqualTo(createAt);
		assertThat(entity.getWithdrawalAt()).isNull();
		assertThat(entity.getTotalPoint()).isEqualTo(totalPoint);
		assertThat(entity.getScore()).isEqualTo(score);
		assertThat(entity.isMatchingEnabled()).isTrue();
	}
}