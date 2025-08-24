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
		// withdrawalAt은 도메인으로 매핑하지 않으므로 null
		assertThat(domain.getWithdrawalAt()).isNull();
		assertThat(domain.getTotalPoint()).isEqualTo(totalPoint);
		assertThat(domain.getScore()).isEqualTo(score);
		assertThat(domain.isMatchingEnabled()).isTrue();
	}

	@Test
	@DisplayName("toNewEntity(): 신규 도메인을 엔티티로 매핑한다 (id/version 미세팅)")
	void toNewEntity_shouldMapDomainToNewEntity() {
		// given (신규 → id=null)
		Long id = null;
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

		MemberProfile profile = new MemberProfile(email, nickname, profileImage, platform, platformId);
		MemberAdditionalInfo info = new MemberAdditionalInfo(phoneNumber, address);
		Member domain = new Member(id, profile, info, createAt, totalPoint, score, true);

		// when
		MemberJpaEntity entity = mapper.toNewEntity(domain);

		// then (신규 → id/version은 아직 없음)
		assertThat(entity.getMemberId()).isNull();
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

	@Test
	@DisplayName("toEntityForUpdate(): 기존 엔티티의 id/version을 이식해 업데이트용 엔티티를 만든다")
	void toEntityForUpdate_shouldMapDomainToEntityWithIdAndVersion() {
		// given (업데이트 시나리오)
		Long id = 20L;
		Long version = 3L;

		MemberJpaEntity current = MemberJpaEntity.builder()
			.memberId(id)
			.version(version) // ★ 기존 버전
			.email("old@example.com")
			.nickname("oldNick")
			.profileImage("oldImg")
			.platform(Platform.GOOGLE)
			.platformId("oldOid")
			.phoneNumber("010-1111-2222")
			.phoneVerified(false)
			.address("OldBusan")
			.createAt(LocalDateTime.of(2025, 7, 10, 10, 0))
			.withdrawalAt(null)
			.totalPoint(10)
			.score(36.5)
			.matchingEnabled(true)
			.build();

		// 변경된 도메인(닉네임/주소/점수 등 일부 변경 가정)
		MemberProfile profile = new MemberProfile("user@example.com", "nick", "img", Platform.GOOGLE, "oid123");
		MemberAdditionalInfo info = new MemberAdditionalInfo("010-0000-0000", "Busan");
		Member domain = new Member(id, profile, info, current.getCreateAt(), 50, 39.5, true);

		// when
		MemberJpaEntity entity = mapper.toEntityForUpdate(domain, current);

		// then (id/version 유지 + 나머지 필드 최신화)
		assertThat(entity.getMemberId()).isEqualTo(id);
		assertThat(entity.getVersion()).isEqualTo(version); // ★ 버전 이식 확인
		assertThat(entity.getEmail()).isEqualTo("user@example.com");
		assertThat(entity.getNickname()).isEqualTo("nick");
		assertThat(entity.getProfileImage()).isEqualTo("img");
		assertThat(entity.getPlatform()).isEqualTo(Platform.GOOGLE);
		assertThat(entity.getPlatformId()).isEqualTo("oid123");
		assertThat(entity.getPhoneNumber()).isEqualTo("010-0000-0000");
		assertThat(entity.isPhoneVerified()).isFalse();
		assertThat(entity.getAddress()).isEqualTo("Busan");
		assertThat(entity.getCreateAt()).isEqualTo(current.getCreateAt());
		assertThat(entity.getWithdrawalAt()).isNull();
		assertThat(entity.getTotalPoint()).isEqualTo(50);
		assertThat(entity.getScore()).isEqualTo(39.5);
		assertThat(entity.isMatchingEnabled()).isTrue();
	}
}