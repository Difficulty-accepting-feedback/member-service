package com.grow.member_service.member.application.service.impl;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.grow.member_service.member.application.service.ScoreUpdateService;
import com.grow.member_service.member.domain.model.MemberScoreInfo;
import com.grow.member_service.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>멤버 신뢰 점수 업데이트 서비스</h2>
 *
 * <p>이 클래스는 멤버의 신뢰 점수를 Redis에 저장하고 업데이트하는 역할을 수행합니다.
 * 개별 멤버의 점수를 저장/업데이트하는 메서드와, 매일 자정에 모든 멤버의 점수를 데이터베이스에서 조회하여
 * Redis에 동기화하는 스케줄링 작업을 포함합니다. Spring의 @Service로 정의되어 비즈니스 로직을 처리하며,
 * RedisTemplate를 통해 Redis와 상호작용합니다.</p>
 *
 * <ol>
 *   <li>의존성: MemberRepository를 통해 데이터베이스에서 점수 조회, RedisTemplate로 Redis 저장/업데이트</li>
 *   <li>주요 메서드:
 *       <ul>
 *         <li>saveMemberScoreToRedis: 단일 멤버의 점수를 Redis에 저장/업데이트 (변경 시에만 적용)</li>
 *         <li>updateAllMemberScores: 매일 자정 스케줄링으로 모든 멤버 점수 동기화</li>
 *       </ul>
 *   </li>
 *   <li>Redis 키: "member:trust:score:{memberId}" 형식으로 사용</li>
 * </ol>
 *
 * @since 2025.07.28
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreUpdateServiceImpl implements ScoreUpdateService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Double> redisTemplate;

    /**
     * Redis 키의 접두사로 사용되는 상수. 멤버의 신뢰 점수를 저장하는 키를 생성합니다.
     */
    public static final String TRUST_KEY = "member:trust:score:";

    /**
     * 단일 멤버의 점수를 Redis에 저장하거나 업데이트하는 메서드.
     * 기존 점수를 확인하고 변경된 경우에만 업데이트합니다.
     *
     * @param memberId 멤버의 ID
     * @param score    저장할 점수 (Double 타입)
     */
    @Override
    public void saveMemberScoreToRedis(Long memberId, Double score) {
        // Redis 키 생성: TRUST_KEY와 memberId를 결합
        String key = TRUST_KEY + memberId;
        Double currentScore = redisTemplate.opsForValue().get(key); // 기존 값 확인

        if (currentScore == null) { // 기존 값이 없으면 새로 저장
            redisTemplate.opsForValue().set(key, score); // Double 타입으로 저장
            log.info("[SCORE SAVE] {} 멤버의 score를 Redis에 저장했습니다.", memberId);
        } else if (!currentScore.equals(score)) { // 변경된 내용이 있는 경우
            redisTemplate.opsForValue().set(key, score);
            log.info("[SCORE UPDATE] {} 멤버의 score를 Redis에 업데이트했습니다.", memberId);
        } else { // 변경된 내용이 없는 경우: 아무 작업도 하지 않음
            log.info("[SCORE SKIP] {} 멤버의 score는 변경되지 않았습니다.", memberId);
        }
    }

    /**
     * Spring의 @Scheduled 어노테이션을 사용하여 매일 자정에 실행되는 스케줄링 메서드.
     * cron 표현식 "0 0 0 * * ?"은 매일 0시 0분 0초를 의미합니다.
     * 모든 멤버의 점수를 데이터베이스에서 조회하여 Redis에 업데이트합니다.
     * <p>
     * TODO: 분산 환경에서 중복 실행을 방지하기 위해 분산 락(Distributed Lock)을 고려
     */

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // 매일 0시 0분 0초 실행
    public void updateAllMemberScores() {
        List<MemberScoreInfo> allScores = memberRepository.findAllScore();// 한 번에 DTO 리스트 받음
        // 실패한 회원들 저장할 큐 - key: memberId, value: score
        ConcurrentLinkedQueue<MemberScoreInfo> failedUpdates = new ConcurrentLinkedQueue<>();

        for (MemberScoreInfo scoreInfo : allScores) {
            try {
                saveMemberScoreToRedis(
                        scoreInfo.getMemberId(),
                        scoreInfo.getScore()
                ); // Redis에 score 업데이트
            } catch (Exception e) {
                failedUpdates.add(scoreInfo); // 실패한 회원들 저장
                log.error("[SCORE SAVE ERROR] {} 멤버의 score를 Redis에 저장하지 못했습니다.",
                        scoreInfo.getMemberId(), e);
            }
        }

        // 모든 작업이 끝난 후 실패 처리
        if (!failedUpdates.isEmpty()) {
            log.warn("[SCORE UPDATE FAILED] 총 {} 명의 멤버의 score를 Redis에 저장하지 못했습니다.",
                    failedUpdates.size());
            retryFailedUpdates(failedUpdates);  // 재시도 메서드 호출
        }
        log.info("[REDIS SCHEDULED] 모든 멤버의 점수를 체크하고 Redis에 업데이트했습니다.");
    }

    /**
     * 실패한 멤버 점수 업데이트 항목들을 재시도하는 메서드입니다.
     * 각 MemberScoreInfo에 대해 최대 3회 재시도를 수행하며, 병렬 스트림을 활용해 분산 환경에서 효율적으로 동작하도록 설계되었습니다.
     * 재시도 실패 시 로그를 남기고, 영구 실패 처리를 위한 TODO를 포함합니다.
     *
     * @param failedUpdates 재시도할 실패한 MemberScoreInfo 항목들의 thread-safe 큐 (ConcurrentLinkedQueue).
     *                      이 큐는 updateAllMemberScores 메서드에서 수집된 실패 항목을 포함합니다.
     * @throws RuntimeException 재시도 중 예상치 못한 오류 발생 시 (예: Redis 연결 영구 실패).
     *
     * @see #updateAllMemberScores() 이 메서드를 호출하는 상위 메서드.
     * @see MemberScoreInfo 재시도 대상 모델 클래스.
     */
    private void retryFailedUpdates(ConcurrentLinkedQueue<MemberScoreInfo> failedUpdates) {
        int maxRetries = 3;

        failedUpdates.parallelStream().forEach(score -> {
            AtomicInteger retryCount = new AtomicInteger(0); // 각 score별 독립 AtomicInteger 설정
            boolean success = false; // 성공 및 실패 여부를 저장할 boolean 변수, false로 초기화

            while (retryCount.get() < maxRetries && !success) {
                try {
                    saveMemberScoreToRedis(score.getMemberId(), score.getScore());
                    success = true;
                    log.info("[SCORE RETRY] {} 멤버의 score를 Redis에 재시도 성공 (시도 횟수: {})",
                            score.getMemberId(), retryCount.get() + 1);  // +1로 실제 시도 횟수 업데이트
                } catch (Exception e) {
                    retryCount.incrementAndGet();  // 실패 시 카운트 증가
                    log.error("[SCORE RETRY ERROR] {} 멤버의 score 재시도 실패 (현재 시도: {}, 오류: {})",
                            score.getMemberId(), retryCount.get(), e.getMessage());
                }
            }

            if (!success) {
                log.error("[SCORE RETRY FAILED] {} 멤버의 업데이트를 최종 실패 (총 시도: {})",
                        score.getMemberId(), maxRetries);
                // TODO: 영구 실패 처리 (DB에 실패 로그 저장, 슬랙 알림 전송, 또는 별도 큐로 이동 등)
            }
        });
    }
}