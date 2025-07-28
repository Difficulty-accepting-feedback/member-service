package com.grow.member_service.member.infra.dto;

/**
 * <h2>멤버 점수 프로젝션 인터페이스</h2>
 *
 * <p>이 인터페이스는 데이터베이스 쿼리에서 멤버의 ID와 신뢰 점수를 Projection하기 위해 사용됩니다.
 * QueryDSL이나 JPA 쿼리에서 필요한 필드만 추출하여 성능을 최적화합니다.
 * MemberRepository의 findAllScore() 메서드에서 DTO로 매핑되어 사용됩니다.</p>
 *
 * <ol>
 *   <li>용도: 데이터베이스에서 memberId와 score 필드를 직접 매핑</li>
 *   <li>메서드:
 *       <ul>
 *         <li>getMemberId(): 멤버의 고유 ID 반환 (Long 타입)</li>
 *         <li>getScore(): 멤버의 신뢰 점수 반환 (Double 타입)</li>
 *       </ul>
 *   </li>
 *   <li>연관: MemberScoreDto로 변환되어 ScoreUpdateService 에서 Redis 업데이트에 활용</li>
 * </ol>
 */
public interface MemberScoreProjection {
    Long getMemberId();
    Double getScore();
}
