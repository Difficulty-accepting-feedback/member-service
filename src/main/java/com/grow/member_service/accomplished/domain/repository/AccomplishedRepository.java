package com.grow.member_service.accomplished.domain.repository;

import com.grow.member_service.accomplished.domain.model.Accomplished;

import java.util.List;
import java.util.Optional;

public interface AccomplishedRepository {
    Accomplished save(Accomplished domain);
    Optional<Accomplished> findById(Long id);
    Optional<Accomplished> findByMemberId(Long memberId);
    List<Accomplished> findAll();
}
