package com.grow.member_service.global.init;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.grow.member_service.challenge.accomplished.domain.model.Accomplished;
import com.grow.member_service.challenge.accomplished.domain.repository.AccomplishedRepository;
import com.grow.member_service.auth.infra.security.jwt.JwtProperties;
import com.grow.member_service.history.point.application.service.PointCommandService;
import com.grow.member_service.history.point.domain.model.PointHistory; // ★ 추가
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.member.application.service.LocationApplicationService;
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

	// ✅ 포인트 정책: 반드시 커맨드 서비스로 적립/차감(원장/스냅샷/멱등/낙관락 처리)
	private final PointCommandService pointCommandService;

	// 지역 업데이트(정적 리졸버 기반 좌표 변환 + Redis GEO 업서트) 선택 주입
	private final ObjectProvider<LocationApplicationService> locationServiceProvider;

	@PostConstruct
	public void init() {
		// 1) 테스트용 단일 회원
		Member testUser = memberRepository.findByPlatformId("test-platform-id", Platform.KAKAO)
			.orElseGet(() -> {
				MemberProfile profile = new MemberProfile(
					"test@kakao.com",
					"TestUser",
					"https://example.com/profile.jpg",
					Platform.KAKAO,
					"test-platform-id"
				);
				MemberAdditionalInfo addInfo = new MemberAdditionalInfo("01012345678", "Test");
				Member m = new Member(profile, addInfo, Clock.systemUTC());
				Member saved = memberRepository.save(m);
				log.info("[INIT] 테스트 계정 생성 - memberId={}, nickname={}", saved.getMemberId(), profile.getNickname());
				return saved;
			});

		// 인증 처리
		if (!testUser.isPhoneVerified()) {
			String phone = testUser.getAdditionalInfo().getPhoneNumber();
			testUser.verifyPhone(phone);
			memberRepository.save(testUser);
			log.info("[INIT] 테스트 계정 휴대폰 인증 처리 완료 - memberId={}", testUser.getMemberId());
		}

		// 테스트 계정도 실제 지역으로 세팅 + GEO 인덱싱 (구리시)
		LocationApplicationService locationSvc = locationServiceProvider.getIfAvailable();
		if (locationSvc != null) {
			try {
				String testRegion = "경기도 구리시";
				locationSvc.updateMyRegion(testUser.getMemberId(), testRegion, null);
				log.info("[INIT] 테스트 계정 지역 인덱싱 완료 - memberId={}, region='{}'",
					testUser.getMemberId(), testRegion);
			} catch (Exception e) {
				log.warn("[INIT] 테스트 계정 지역 인덱싱 실패 - memberId={}, err={}",
					testUser.getMemberId(), e.toString());
			}
		} else {
			log.info("[INIT] LocationApplicationService 미주입 - 테스트 계정 지역 인덱싱 스킵");
		}

		long memberId = testUser.getMemberId();
		Clock clock = Clock.systemUTC();

		// 2) 포인트 더미
		if (pointHistoryRepository.findByMemberId(memberId).isEmpty()) {
			IntStream.rangeClosed(1, 60).forEach(i -> {
				int amount = (i % 5 == 0) ? -200 : ((i % 2 == 0) ? +100 : +50);
				String content = "모의 포인트 내역 #" + i + (amount > 0 ? " 적립" : " 사용");
				String dedupKey = "INIT_SEED:" + memberId + ":" + i;

				try {
					PointHistory ph;
					if (amount >= 0) {
						ph = pointCommandService.grant(
							memberId, amount,
							PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
							"seed-" + i, content, dedupKey, LocalDateTime.now(clock)
						);
						log.info("[INIT] 포인트 시드 적립 완료 - memberId={}, i={}, amount=+{}, balanceAfter={}, historyId={}, dedupKey={}",
							memberId, i, amount,
							ph.getBalanceAfter(), ph.getPointHistoryId(), ph.getDedupKey());
					} else {
						int use = Math.abs(amount);
						ph = pointCommandService.spend(
							memberId, use,
							PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
							"seed-" + i, content, dedupKey, LocalDateTime.now(clock)
						);
						log.info("[INIT] 포인트 시드 차감 완료 - memberId={}, i={}, amount=-{}, balanceAfter={}, historyId={}, dedupKey={}",
							memberId, i, use,
							ph.getBalanceAfter(), ph.getPointHistoryId(), ph.getDedupKey());
					}
				} catch (Exception ex) {
					log.warn("[INIT] 포인트 시드 실패 - memberId={}, i={}, amount={}, err={}",
						memberId, i, amount, ex.toString());
				}
			});

			long seeded = pointHistoryRepository.findByMemberId(memberId).size();
			log.info("[INIT] 테스트용 포인트 내역 시드 완료 - {}건", seeded);

			// ★ 시드 적립 후 일부 차감(샘플)
			seedInitialDebits(memberId, clock);
		}

		// 3) 업적 더미
		if (accomplishedRepository.findAllByMemberId(memberId).isEmpty()) {
			IntStream.rangeClosed(1, 60).forEach(i -> {
				Accomplished acc = new Accomplished(memberId, (long) i, LocalDateTime.now(clock));
				accomplishedRepository.save(acc);
			});
			log.info("[INIT] 테스트용 업적 60건 생성 완료");
		}

		// 4) 퀴즈 결과 더미
		if (quizResultRepository.findByMemberId(memberId).isEmpty()) {
			IntStream.rangeClosed(1, 20).forEach(i -> {
				long quizId = (i % 5) + 1;
				boolean isCorrect = (i % 3 != 0); // 3의 배수만 틀리게
				quizResultRepository.save(new QuizResult(memberId, quizId, isCorrect));
			});
			log.info("[INIT] 테스트용 퀴즈 결과 20건 생성 완료");
		}

		// 5) 더미 멤버 10명 (서울 8개 구 + 경기 2개 시)
		List<String> regions = List.of(
			"서울특별시 강남구",
			"서울특별시 서초구",
			"서울특별시 노원구",
			"서울특별시 성북구",
			"서울특별시 강북구",
			"서울특별시 강동구",
			"서울특별시 도봉구",
			"서울특별시 송파구",
			"경기도 구리시",
			"경기도 성남시"
		);

		int created = 0;
		for (int i = 0; i < regions.size(); i++) {
			String region = regions.get(i);
			String platformId = "dummy-platform-" + (i + 1);
			Platform platform = (i % 2 == 0) ? Platform.GOOGLE : Platform.KAKAO;
			String email = "dummy" + (i + 1) + "@example.com";
			String nickname = "더미" + (i + 1);

			Member existing = memberRepository.findByPlatformId(platformId, platform).orElse(null);
			if (existing == null) {
				MemberProfile p = new MemberProfile(email, nickname, null, platform, platformId);
				MemberAdditionalInfo a = new MemberAdditionalInfo(null, region);
				Member m = new Member(p, a, Clock.systemUTC());
				existing = memberRepository.save(m);
				created++;
				log.info("[INIT] 더미 멤버 생성 - memberId={}, nickname={}, region='{}'",
					existing.getMemberId(), nickname, region);
			} else {
				log.info("[INIT] 더미 멤버 존재 - memberId={}, nickname={}, region(현재)='{}'",
					existing.getMemberId(), existing.getMemberProfile().getNickname(),
					existing.getAdditionalInfo().getAddress());
			}

			if (locationSvc != null) {
				try {
					locationSvc.updateMyRegion(existing.getMemberId(), region, null);
					log.info("[INIT] 더미 멤버 지역 인덱싱 완료 - memberId={}, region='{}'",
						existing.getMemberId(), region);
				} catch (Exception e) {
					log.warn("[INIT] 더미 멤버 지역 인덱싱 실패 - memberId={}, region='{}', err={}",
						existing.getMemberId(), region, e.toString());
				}
			} else {
				log.info("[INIT] LocationApplicationService 미주입 - 더미 지역 인덱싱 스킵 - memberId={}",
					existing.getMemberId());
			}
		}
		log.info("[INIT] 더미 멤버 생성 완료 - 신규 {}명 / 총 시도 {}건", created, regions.size());

		// 6) 토큰 생성 (테스트 계정용)
		SecretKey key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
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

	/** ★ 시드 적립 후 일부 차감(샘플) */
	private void seedInitialDebits(long memberId, Clock clock) {
		int[] debits = {150, 75};
		for (int idx = 0; idx < debits.length; idx++) {
			int amt = debits[idx];
			String dedupKey = "INIT_DEBIT:" + memberId + ":" + (idx + 1);
			try {
				PointHistory ph = pointCommandService.spend(
					memberId, amt,
					PointActionType.ADMIN_ADJUST, SourceType.SYSTEM,
					"debit-seed-" + (idx + 1),
					"초기 차감 시드 #" + (idx + 1),
					dedupKey,
					LocalDateTime.now(clock)
				);
				log.info("[INIT] 포인트 초기 차감 완료 - memberId={}, idx={}, amount=-{}, balanceAfter={}, historyId={}, dedupKey={}",
					memberId, (idx + 1), amt,
					ph.getBalanceAfter(), ph.getPointHistoryId(), ph.getDedupKey());
			} catch (Exception ex) {
				log.warn("[INIT] 포인트 초기 차감 실패 - memberId={}, idx={}, amount=-{}, err={}",
					memberId, (idx + 1), amt, ex.toString());
			}
		}
	}
}