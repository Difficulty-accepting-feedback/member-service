package com.grow.member_service.member.application.service;

import com.grow.member_service.member.domain.model.MemberScoreInfo;
import com.grow.member_service.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

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
public class ScoreUpdateService {

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
     * @param score 저장할 점수 (Double 타입)
     */
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
     *
     * TODO: 분산 환경에서 중복 실행을 방지하기 위해 분산 락(Distributed Lock)을 고려
     */
    // 매일 자정에 모든 멤버의 score를 조건부 업데이트하는 스케줄러
    @Scheduled(cron = "0 0 0 * * ?") // 매일 0시 0분 0초 실행
    public void updateAllMemberScores() {
        List<MemberScoreInfo> allScores = memberRepository.findAllScore();// 한 번에 DTO 리스트 받음
        for (MemberScoreInfo dto : allScores) {
            saveMemberScoreToRedis(dto.getMemberId(), dto.getScore()); // Redis에 score 업데이트
        }
        log.info("[REDIS SCHEDULED] 모든 멤버의 점수를 체크하고 Redis에 업데이트했습니다.");
    }
}