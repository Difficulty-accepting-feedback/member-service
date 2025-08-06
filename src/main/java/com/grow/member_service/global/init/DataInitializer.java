package com.grow.member_service.global.init;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.grow.member_service.accomplished.domain.model.Accomplished;
import com.grow.member_service.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.quiz.result.domain.model.QuizResult;
import com.grow.member_service.quiz.result.domain.repository.QuizResultRepository;

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
	private final PointHistoryRepository pointHistoryRepository;
	private final AccomplishedRepository accomplishedRepository;
	private final QuizResultRepository quizResultRepository;
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
		Clock clock = Clock.systemUTC();

		if (pointHistoryRepository.findByMemberId(memberId).isEmpty()) {
			List<PointHistory> mocks = IntStream.rangeClosed(1, 60)
				.mapToObj(i -> {
					// 홀수는 +50, 짝수는 +100, 5의 배수는 -200 차감
					int amount = (i % 5 == 0)
						? -200
						: ((i % 2 == 0) ? +100 : +50);
					String content = "모의 포인트 내역 #" + i
						+ (amount > 0 ? " 적립" : " 사용");
					return new PointHistory(memberId, amount, content, clock);
				})
				.collect(Collectors.toList());

			mocks.forEach(pointHistoryRepository::save);
			log.info("테스트용 포인트 내역 {}건 생성 완료", mocks.size());
		}

		if (accomplishedRepository.findAllByMemberId(memberId).isEmpty()) {
			List<Accomplished> accomplishments = IntStream.rangeClosed(1, 60)
				.mapToObj(i -> {
					LocalDateTime now = LocalDateTime.now(clock);
					return new Accomplished(memberId, (long) i, now);
				})
				.collect(Collectors.toList());

			accomplishments.forEach(accomplishedRepository::save);
			log.info("테스트용 업적 {}건 생성 완료", accomplishments.size());
		}

		if (quizResultRepository.findByMemberId(memberId).isEmpty()) {
			List<QuizResult> quizResults = IntStream.rangeClosed(1, 20)
				.mapToObj(i -> {
					long quizId = (i % 5) + 1;
					boolean isCorrect = (i % 3 != 0);             // 3의 배수만 틀리게
					return new QuizResult(memberId, quizId, isCorrect);
				})
				.collect(Collectors.toList());

			quizResults.forEach(quizResultRepository::save);
			log.info("테스트용 퀴즈 결과 {}건 생성 완료", quizResults.size());
		}


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