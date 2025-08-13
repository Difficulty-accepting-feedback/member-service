package com.grow.member_service.member.application.service;

import java.util.List;

import com.grow.member_service.member.application.dto.NearbyMemberResponse;

public interface LocationApplicationService {

	/** 멤버의 지역을 업데이트합니다. 주소<->좌표 변환 + GEO 업서트 */
	void updateMyRegion(Long memberId, String regionText, String sggCode);

	/** 인근 조회 */
	List<NearbyMemberResponse> findNearby(double lat, double lng, int km, int limit);

	/** 멤버를 중심으로 반경 조회 */
	List<NearbyMemberResponse> findNearbyOf(long centerMemberId, int km, int limit);
}