package com.grow.member_service.member.infra.mapper;

import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.infra.entity.MemberEntity;
import org.springframework.stereotype.Component;

/**
 * 도메인 - 엔티티 변환 클래스
 */
@Component
public class MemberMapper {

    // 엔티티를 도메인으로 변환
    public Member toDomain(MemberEntity entity) {
        return null;
    }

    // 도메인을 엔티티로 변환
    public MemberEntity toEntity(Member domain) {
        return null;
    }
}
