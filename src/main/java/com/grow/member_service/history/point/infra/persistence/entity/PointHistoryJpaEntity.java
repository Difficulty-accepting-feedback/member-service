package com.grow.member_service.history.point.infra.persistence.entity;

import java.time.LocalDateTime;

import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "pointHistory",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_point_history_dedup", columnNames = "dedupKey")
    },
    indexes = {
        @Index(name = "idx_ph_member_addAt", columnList = "memberId, addAt DESC")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pointHistoryId", nullable = false, updatable = false)
    private Long pointHistoryId;

    @Column(name = "memberId", nullable = false, updatable = false)
    private Long memberId;

    @Column(name = "amount", nullable = false, updatable = false)
    private Integer amount;

    @Column(name = "content", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "addAt", nullable = false, updatable = false)
    private LocalDateTime addAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "actionType", length = 40, updatable = false)
    private PointActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sourceType", length = 40, updatable = false)
    private SourceType sourceType;

    @Column(name = "sourceId", length = 100, updatable = false)
    private String sourceId;

    @Column(name = "dedupKey", length = 150, updatable = false)
    private String dedupKey;

    @Column(name = "balanceAfter", updatable = false)
    private Long balanceAfter;

    @Builder
    public PointHistoryJpaEntity(Long memberId,
        Integer amount,
        String content,
        LocalDateTime addAt,
        PointActionType actionType,
        SourceType sourceType,
        String sourceId,
        String dedupKey,
        Long balanceAfter) {
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;
        this.addAt = addAt;
        this.actionType = actionType;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.dedupKey = dedupKey;
        this.balanceAfter = balanceAfter;
    }
}