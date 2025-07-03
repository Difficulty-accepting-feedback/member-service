package com.grow.member_service.history.point.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "pointHistory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pointHistoryId", nullable = false, updatable = false)
    private Long pointHistoryId;

    @Column(name = "memberId",  nullable = false, updatable = false)
    private Long memberId; // 멤버 ID

    @Column(name = "amount",  nullable = false, updatable = false)
    private Integer amount; // 포인트 적립 or 사용 금액 (단건)

    @Column(name = "content",  nullable = false, updatable = false, columnDefinition = "TEXT")
    private String content; // 상세 내용

    @Column(name = "addAt",  nullable = false, updatable = false)
    private LocalDateTime addAt; // 적립 or 사용 날짜

    @Builder
    public PointHistoryJpaEntity(Long memberId,
                                 Integer amount,
                                 String content,
                                 LocalDateTime addAt
    ) {
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;
        this.addAt = addAt;
    }
}
