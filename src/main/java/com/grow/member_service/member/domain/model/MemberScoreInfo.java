package com.grow.member_service.member.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * <h2>멤버 점수 VO</h2>
 *
 * <p>이 클래스는 멤버의 ID와 신뢰 점수를 전달하기 위한 Value Object 입니다.
 * 데이터베이스 조회 결과를 캡슐화하여 서비스 계층 ScoreUpdateService 에서 Redis 업데이트 시 활용됩니다.</p>
 *
 * <ol>
 *   <li>필드:
 *       <ul>
 *         <li>memberId: 멤버의 고유 ID (Long 타입)</li>
 *         <li>score: 멤버의 신뢰 점수 (Double 타입)</li>
 *       </ul>
 *   </li>
 *   <li>용도: MemberRepository의 Projection 결과를 매핑하여 처리 (Projection -> 인프라 레이어의 DTO)</li>
 * </ol>
 */
public class MemberScoreInfo {

    private final Long memberId;
    private final Double score;

    public MemberScoreInfo(Long memberId, Double score) {
        this.memberId = memberId;
        this.score = score;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "MemberScoreInfo{" +
                "memberId=" + memberId +
                ", score=" + score +
                '}';
    }
}
