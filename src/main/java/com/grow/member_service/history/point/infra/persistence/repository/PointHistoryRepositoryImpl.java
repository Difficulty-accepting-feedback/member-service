package com.grow.member_service.history.point.infra.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.grow.member_service.history.point.domain.model.PointHistory;
import com.grow.member_service.history.point.domain.model.enums.PointActionType;
import com.grow.member_service.history.point.domain.repository.PointHistoryRepository;
import com.grow.member_service.history.point.infra.persistence.entity.PointHistoryJpaEntity;
import com.grow.member_service.history.point.infra.persistence.mapper.PointHistoryMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository jpaRepository;
    private final PointHistoryMapper mapper;

    /**
     * 포인트 기록을 저장합니다.
     * @param pointHistory 저장할 포인트 기록 객체
     * @return 저장된 포인트 기록 객체
     */
    @Override
    public PointHistory save(PointHistory pointHistory) {
        PointHistoryJpaEntity entity = mapper.toEntity(pointHistory);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    /**
     * 특정 회원의 포인트 기록을 조회합니다.
     * @param id 회원 ID
     * @return 포인트 기록 리스트
     */
    @Override
    public List<PointHistory> findByMemberId(Long id) {
        return jpaRepository.findByMemberId(id)
                .stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 특정 회원의 포인트 기록을 페이징하여 조회합니다.
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 포인트 기록 페이지
     */
    @Override
    public Page<PointHistory> findByMemberId(Long memberId, Pageable pageable) {
        return jpaRepository.findByMemberId(memberId, pageable)
            .map(mapper::toDomain);
    }

    /**
     * 특정 회원의 포인트 기록을 기간별로 조회합니다.
     * @param memberId 회원 ID
     * @param startAt 조회 시작 시각
     * @param endAt 조회 종료 시각
     * @param pageable 페이징 정보
     * @return 포인트 기록 페이지
     */
    @Override
    public Page<PointHistory> findByMemberIdAndPeriod(
        Long memberId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Pageable pageable
    ) {
        return jpaRepository.findByMemberIdAndAddAtBetween(memberId, startAt, endAt, pageable)
            .map(mapper::toDomain);
    }

    /**
     * 포인트 기록을 삭제합니다.
     * @param pointHistory 삭제할 포인트 기록 객체
     */
    @Override
    public void delete(PointHistory pointHistory) {
        jpaRepository.delete(mapper.toEntity(pointHistory));
    }

    /**
     * 멱등 키로 포인트 기록을 조회합니다.
     * @param dedupKey 멱등 키
     * @return 포인트 기록 객체 (존재하지 않으면 Optional.empty())
     */
    @Override
    public Optional<PointHistory> findByDedupKey(String dedupKey) {
        return jpaRepository.findByDedupKey(dedupKey).map(mapper::toDomain);
    }

    /**
     * 특정 회원의 포인트 기록을 액션 타입과 기간으로 카운트합니다.
     * @param memberId 회원 ID
     * @param actionType 포인트 액션 타입
     * @param startAt 조회 시작 시각
     * @param endAt 조회 종료 시각
     * @return 포인트 기록 개수
     */
    @Override
    public long countByMemberIdAndActionTypeBetween(Long memberId, PointActionType actionType, LocalDateTime startAt, LocalDateTime endAt) {
        return jpaRepository.countByMemberIdAndActionTypeBetween(memberId, actionType, startAt, endAt);
    }
}