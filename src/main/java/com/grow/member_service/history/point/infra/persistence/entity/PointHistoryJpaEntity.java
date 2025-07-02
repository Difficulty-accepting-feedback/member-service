package com.grow.member_service.history.point.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "pointHistory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pointHistoryId", nullable = false)
    private Long pointHistoryId;

    private Long memberId; // 멤버 ID

    private Integer amount; // 포인트 적립 or 사용 금액 (단건)

    private String content; // 상세 내용

    private LocalDateTime addedAt; // 적립 or 사용 날짜

    @Builder
    public PointHistoryJpaEntity(Long memberId,
                                 int amount,
                                 String content,
                                 Clock addedAt
    ) {
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;

        if (addedAt != null) this.addedAt = LocalDateTime.now(addedAt);
        else this.addedAt = LocalDateTime.now();
    }
}
