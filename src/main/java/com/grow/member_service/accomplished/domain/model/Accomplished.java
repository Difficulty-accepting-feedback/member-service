package com.grow.member_service.accomplished.domain.model;

import lombok.Getter;

@Getter
public class Accomplished {

    private Long accomplishedId;
    private Long memberId;
    private Long challengeId;

    public Accomplished(Long memberId,
                        Long challengeId
    ) {
        this.accomplishedId = null; // 생성 시 null, 저장 후 설정
        this.memberId = memberId;
        this.challengeId = challengeId;
    }

    public Accomplished(Long accomplishedId,
                        Long memberId,
                        Long challengeId
    ) {
        this.accomplishedId = accomplishedId;
        this.memberId = memberId;
        this.challengeId = challengeId;
    }

    /**
     * 비즈니스 로직 메서드
     */
}
