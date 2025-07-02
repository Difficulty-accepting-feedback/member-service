package com.grow.member_service.history.point.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointHistory {

    private final Long pointHistoryId;
    private final Long memberId;
    private final Integer amount;
    private final String content;
    private final LocalDateTime addedAt;

    public PointHistory(Long memberId,
                        Integer amount,
                        String content
    ) {
        this.pointHistoryId = null; // DB 저장 후 자동으로 생성
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;
        this.addedAt = null; // DB 저장 후 자동으로 생성
    }
}
