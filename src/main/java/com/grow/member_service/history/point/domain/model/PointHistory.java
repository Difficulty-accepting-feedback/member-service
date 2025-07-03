package com.grow.member_service.history.point.domain.model;

import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
public class PointHistory {

    private final Long pointHistoryId;
    private final Long memberId;
    private final Integer amount;
    private final String content;
    private final LocalDateTime addAt;

    public PointHistory(Long memberId,
                        Integer amount,
                        String content,
                        Clock addAt
    ) {
        this.pointHistoryId = null; // DB 저장 후 자동으로 생성
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;

        if (addAt != null) {
            this.addAt = LocalDateTime.now(addAt);
        } else  {
            this.addAt = LocalDateTime.now();
        }
    }

    public PointHistory(Long pointHistoryId,
                        Long memberId,
                        Integer amount,
                        String content,
                        LocalDateTime addAt
    ) {
        this.pointHistoryId = pointHistoryId;
        this.memberId = memberId;
        this.amount = amount;
        this.content = content;
        this.addAt = addAt;
    }
}
