package com.grow.member_service.member.application.port;

import java.util.List;

public interface GeoIndexPort {

	/** 좌표 인덱스 갱신(업서트) */
	void upsert(long memberId, double lat, double lng);

	/** 좌표 인덱스 삭제 */
	void remove(long memberId);

	/** WITHDIST 결과 */
	record GeoHit(Long memberId, double distanceKm) {}

	/** 좌표 기준 반경 검색(거리 오름차순, km 단위) */
	List<GeoHit> search(double lat, double lng, double radiusMeters, int limit);

	/** 특정 멤버를 중심으로 반경 검색(거리 오름차순, km 단위) */
	List<GeoHit> searchByMember(long centerMemberId, double radiusMeters, int limit);
}