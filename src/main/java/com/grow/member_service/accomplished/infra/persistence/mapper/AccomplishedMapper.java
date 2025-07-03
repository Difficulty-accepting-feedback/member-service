package com.grow.member_service.accomplished.infra.persistence.mapper;

import com.grow.member_service.accomplished.domain.model.Accomplished;
import com.grow.member_service.accomplished.infra.persistence.entity.AccomplishedJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AccomplishedMapper {

    // 엔티티를 도메인으로 변환
    public Accomplished toDomain(AccomplishedJpaEntity entity) {
        return new Accomplished(
                entity.getAccomplishedId(),
                entity.getMemberId(),
                entity.getChallengeId()
        );
    }

    // 도메인을 엔티티로 변환
    public AccomplishedJpaEntity toEntity(Accomplished domain) {
        return new AccomplishedJpaEntity(
                domain.getMemberId(),
                domain.getAccomplishedId()
        );
    }
}
