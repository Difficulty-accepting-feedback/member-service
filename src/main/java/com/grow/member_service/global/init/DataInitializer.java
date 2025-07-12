package com.grow.member_service.global.init;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

	private final MemberRepository memberRepository;
	private final JwtProperties props;

	@PostConstruct
	public void init() {
		// 테스트용 회원
		Member testUser = memberRepository.findByPlatformId("test-platform-id", Platform.KAKAO)
			.orElseGet(() -> {
				MemberProfile profile = new MemberProfile(
					"test@kakao.com",
					"TestUser",
					"https://example.com/profile.jpg",
					Platform.KAKAO,
					"test-platform-id"
				);
				MemberAdditionalInfo addInfo = new MemberAdditionalInfo(
					"01012345678",
					"Test"
				);
				Member m = new Member(profile, addInfo, Clock.systemUTC());
				return memberRepository.save(m);
			});

		// 인증 처리
		if (!testUser.isPhoneVerified()) {
			String phone = testUser.getAdditionalInfo().getPhoneNumber();
			testUser.verifyPhone(phone);
			memberRepository.save(testUser);
		}

		long memberId = testUser.getMemberId();

		// 토큰 생성
		SecretKey key = Keys.hmacShaKeyFor(
			props.getSecret().getBytes(StandardCharsets.UTF_8)
		);

		Date issuedAt = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
		Date expiration = Date.from(Instant.parse("2030-01-01T00:00:00Z"));

		String token = Jwts.builder()
			.setClaims(Map.of("memberId", memberId))
			.setSubject(String.valueOf(memberId))
			.setIssuedAt(issuedAt)
			.setExpiration(expiration)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		log.info("테스트용 멤버 ID    : {}", memberId);
		log.info("테스트용 액세스 토큰 : {}", token);
	}
}