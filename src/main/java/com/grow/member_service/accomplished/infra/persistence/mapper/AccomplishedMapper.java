package com.grow.member_service.accomplished.infra.persistence.mapper;

import org.springframework.stereotype.Component;

import com.grow.member_service.accomplished.domain.model.Accomplished;
import com.grow.member_service.accomplished.infra.persistence.entity.AccomplishedJpaEntity;

@Component
public class AccomplishedMapper {

    // 엔티티를 도메인으로 변환
    public Accomplished toDomain(AccomplishedJpaEntity entity) {
        return new Accomplished(
            entity.getAccomplishedId(),
            entity.getMemberId(),
            entity.getChallengeId(),
            entity.getAccomplishedAt()
        );
    }

    // 도메인을 엔티티로 변환
    public AccomplishedJpaEntity toEntity(Accomplished d) {
        return AccomplishedJpaEntity.builder()
            .memberId(d.getMemberId())
            .challengeId(d.getChallengeId())
            .accomplishedAt(d.getAccomplishedAt())
            .build();
    }
}