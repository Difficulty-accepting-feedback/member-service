package com.grow.member_service.history.point.domain.model;

import java.time.Clock;
import java.time.LocalDateTime;

import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.model.enums.SourceType;

import lombok.Getter;

@Getter
public class PointHistory {

    private final Long pointHistoryId;
    private final Long memberId;
    private final Integer amount;
    private final String content;
    private final LocalDateTime addAt;
    private final PointActionType actionType; // 포인트 적립/사용 액션 타입
    private final SourceType sourceType; // 포인트 발생 소스 타입 (예: 출석, 게시판 등)
    private final String sourceId; // 포인트 발생 소스 ID (예: 출석 ID, 게시판 글 ID 등)
    private final String dedupKey; // 멱등성 키 (중복 방지용)
    private final Long balanceAfter; // 포인트 사용 후 잔액

    public PointHistory(Long memberId, Integer amount, String content, Clock addAt) {
        this.pointHistoryId = null;
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;
        this.addAt = (addAt != null) ? LocalDateTime.now(addAt) : LocalDateTime.now();
        this.actionType = null;
        this.sourceType = null;
        this.sourceId = null;
        this.dedupKey = null;
        this.balanceAfter = null;
    }

    public PointHistory(Long pointHistoryId, Long memberId, Integer amount, String content, LocalDateTime addAt) {
        this.pointHistoryId = pointHistoryId;
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;
        this.addAt = addAt;
        this.actionType = null;
        this.sourceType = null;
        this.sourceId = null;
        this.dedupKey = null;
        this.balanceAfter = null;
    }

    public PointHistory(Long pointHistoryId,
        Long memberId,
        Integer amount,
        String content,
        LocalDateTime addAt,
        PointActionType actionType,
        SourceType sourceType,
        String sourceId,
        String dedupKey,
        Long balanceAfter) {
        this.pointHistoryId = pointHistoryId;
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