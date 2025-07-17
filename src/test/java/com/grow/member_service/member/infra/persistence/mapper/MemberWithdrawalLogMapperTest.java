package com.grow.member_service.member.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.grow.member_service.member.domain.model.MemberWithdrawalLog;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.infra.persistence.entity.MemberWithdrawalLogJpaEntity;

class MemberWithdrawalLogMapperTest {

	private MemberWithdrawalLogMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new MemberWithdrawalLogMapper();
	}

	@Test
	@DisplayName("toEntity(): 도메인 → JPA 엔티티 매핑 확인")
	void toEntity_shouldMapDomainToEntity() {
		// given
		Long memberId = 42L;
		String email = "user@example.com";
		String nickname = "nick42";
		Platform platform = Platform.KAKAO;
		String platformId = "ext-42";
		String phoneNumber = "010-4242-4242";
		LocalDateTime withdrawnAt = LocalDateTime.of(2025, 7, 17, 15, 30);

		MemberWithdrawalLog domain = new MemberWithdrawalLog(
			memberId,
			email,
			nickname,
			platform,
			platformId,
			phoneNumber,
			withdrawnAt
		);

		// when
		MemberWithdrawalLogJpaEntity entity = mapper.toEntity(domain);

		// then
		assertThat(entity.getMemberId()).isEqualTo(memberId);
		assertThat(entity.getEmail()).isEqualTo(email);
		assertThat(entity.getNickname()).isEqualTo(nickname);
		assertThat(entity.getPlatform()).isEqualTo(platform);
		assertThat(entity.getPlatformId()).isEqualTo(platformId);
		assertThat(entity.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(entity.getWithdrawnAt()).isEqualTo(withdrawnAt);
	}

	@Test
	@DisplayName("toDomain(): JPA 엔티티 → 도메인 매핑 확인")
	void toDomain_shouldMapEntityToDomain() {
		// given
		Long memberId = 99L;
		String email = "foo@bar.com";
		String nickname = "foobar";
		Platform platform = Platform.GOOGLE;
		String platformId = "ext-99";
		String phoneNumber = "010-9999-9999";
		LocalDateTime withdrawnAt = LocalDateTime.of(2025, 8, 1, 12, 0);

		MemberWithdrawalLogJpaEntity entity = MemberWithdrawalLogJpaEntity.builder()
			.memberId(memberId)
			.email(email)
			.nickname(nickname)
			.platform(platform)
			.platformId(platformId)
			.phoneNumber(phoneNumber)
			.withdrawnAt(withdrawnAt)
			.build();

		// when
		MemberWithdrawalLog domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getMemberId()).isEqualTo(memberId);
		assertThat(domain.getOriginalEmail()).isEqualTo(email);
		assertThat(domain.getOriginalNickname()).isEqualTo(nickname);
		assertThat(domain.getPlatform()).isEqualTo(platform);
		assertThat(domain.getOriginalPlatformId()).isEqualTo(platformId);
		assertThat(domain.getOriginalPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(domain.getWithdrawnAt()).isEqualTo(withdrawnAt);
	}
}