package com.grow.member_service.member.application.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grow.member_service.common.exception.LocationException;
import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.member.application.dto.NearbyMemberResponse;
import com.grow.member_service.member.application.port.GeoIndexPort;
import com.grow.member_service.member.application.port.GeocodingPort;
import com.grow.member_service.member.application.service.LocationApplicationService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.infra.region.RegionCenterResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GROW - 위치/매칭 유스케이스
 * - 주소 변경: 정적 SGG 중심좌표 → Redis GEO 업서트
 * - 인근 조회: 최소 후보 수 확보까지 반경 점진 확대(base → +5 → +10 → +15), 거리 포함 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationApplicationServiceImpl implements LocationApplicationService {

	private final MemberRepository memberRepository;
	private final ObjectProvider<GeocodingPort> geocodingProvider;
	private final ObjectProvider<GeoIndexPort> geoIndexProvider;
	private final RegionCenterResolver regionResolver;

	private static final int MIN_CANDIDATES = 20;       // 최소 확보 인원
	private static final int[] EXPAND_OFFSETS_KM = {0, 5, 10, 15}; // base, +5, +10, +15
	private static final int MAX_RADIUS_CAP_KM = 30;    // 최대 반경
	private static final int CENTROID_BONUS_KM = 2;     // 시군구 중심점 오차 보정

	/**
	 * 사용자의 표시용 주소를 저장하고, 좌표를 산출하여 Redis GEO 인덱스에 업서트한다.
	 *
	 * <ul>
	 *   <li>주소 텍스트는 공백 정규화 후 멤버 프로필에 저장(변경 시에만).</li>
	 *   <li>좌표는 sggCode가 있으면 코드→좌표 매핑을 우선 사용, 없으면 텍스트→좌표 매핑을 사용.</li>
	 *   <li>지오 인덱스 포트가 미주입된 경우 좌표 업서트는 스킵.</li>
	 * </ul>
	 *
	 * @param memberId  대상 멤버 ID(인증 주체)
	 * @param regionText 시/군/구 라벨(예: "서울특별시 강남구")
	 * @param sggCode    시군구 코드(예: "11680"), 있으면 코드 매핑 우선
	 * @throws IllegalArgumentException regionText가 비어있거나 멤버/코드가 존재하지 않는 경우
	 */
	@Transactional
	@Override
	public void updateMyRegion(Long memberId, String regionText, String sggCode) {
		log.info("[MEMBER][LOCATION] 지역 업데이트 요청 - memberId={}, region='{}', sggCode={}", memberId, regionText, sggCode);

		if (regionText == null || regionText.isBlank()) {
			log.warn("[MEMBER][LOCATION] 지역 업데이트 거부 - memberId={}, 사유=빈 문자열/NULL", memberId);
			throw new LocationException(ErrorCode.REGION_BLANK);
		}

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> {
				log.warn("[MEMBER][LOCATION] 지역 업데이트 실패 - memberId={}, 사유=멤버 미존재", memberId);
				return new MemberException(ErrorCode.MEMBER_NOT_FOUND);
			});

		boolean changed = member.changeAddressIfDifferent(regionText);
		if (changed) {
			memberRepository.save(member);
			log.info("[MEMBER][LOCATION] 지역 저장 완료 - memberId={}, address='{}'", memberId, member.getAdditionalInfo().getAddress());
		} else {
			log.debug("[MEMBER][LOCATION] 지역 저장 스킵 - memberId={}, 동일 주소/빈값", memberId);
		}

		// 좌표 변환 + Redis GEO 업서트
		GeoIndexPort geoIndex = geoIndexProvider.getIfAvailable();
		if (geoIndex == null) {
			log.debug("[MEMBER][LOCATION] GEO 업서트 스킵 - memberId={}, 사유=GeoIndex 포트 미주입", memberId);
			return;
		}

		Double lat; Double lng;
		if (sggCode != null && !sggCode.isBlank()) {
			var byCode = regionResolver.resolveBySggCode(sggCode);
			if (byCode == null) {
				log.warn("[MEMBER][LOCATION] 좌표 변환 실패 - memberId={}, sggCode='{}' 매핑 없음", memberId, sggCode);
				throw new LocationException(ErrorCode.UNKNOWN_SGG_CODE);
			}
			lat = byCode.lat();
			lng = byCode.lon();
			log.info("[MEMBER][LOCATION] 좌표 변환(코드 기반) 완료 - memberId={}, sggCode={}, lat={}, lng={}", memberId, sggCode, lat, lng);
		} else {
			GeocodingPort geocoder = geocodingProvider.getIfAvailable();
			if (geocoder == null) {
				log.debug("[MEMBER][LOCATION] 지오코딩 스킵 - memberId={}, 사유=Geocoding 포트 미주입", memberId);
				return;
			}
			GeocodingPort.LatLng ll = geocoder.geocodeRegion(member.getAdditionalInfo().getAddress());
			lat = ll.lat(); lng = ll.lng();
			log.info("[MEMBER][LOCATION] 좌표 변환(텍스트 기반) 완료 - memberId={}, lat={}, lng={}", memberId, lat, lng);
		}

		geoIndex.upsert(memberId, lat, lng);
		log.info("[MEMBER][LOCATION] GEO 업서트 완료 - memberId={}, key=geo:home", memberId);
	}
	/**
	 * 좌표(lat,lng)를 중심으로 인근 멤버를 조회한다.
	 * <p>요청 반경을 기준으로 오차 보정(+2km) 후, 단계적으로 반경을 확장하여
	 * 최소 후보 수를 확보한다. 결과는 거리 오름차순으로 최대 {@code limit}명 반환한다.</p>
	 *
	 * @param lat   중심 위도
	 * @param lng   중심 경도
	 * @param km    기본 반경(km)
	 * @param limit 최대 반환 개수
	 * @return 가까운 순의 멤버 응답 리스트(0~limit)
	 */
	@Transactional(readOnly = true)
	@Override
	public List<NearbyMemberResponse> findNearby(double lat, double lng, int km, int limit) {
		log.info("[MEMBER][LOCATION] 인근 멤버 조회 요청 - lat={}, lng={}, km={}, limit={}", lat, lng, km, limit);

		GeoIndexPort geoIndex = geoIndexProvider.getIfAvailable();
		if (geoIndex == null) {
			log.warn("[MEMBER][LOCATION] 인근 조회 스킵 - 사유=GeoIndex 포트 미주입");
			return List.of();
		}

		List<GeoIndexPort.GeoHit> hits = progressiveSearchByPoint(
			geoIndex, lat, lng, km, limit, MIN_CANDIDATES
		);
		if (hits.isEmpty()) {
			log.info("[MEMBER][LOCATION] 인근 조회 결과 - 후보 없음");
			return List.of();
		}

		List<NearbyMemberResponse> out = buildNearbyDtos(hits, limit);
		log.info("[MEMBER][LOCATION] 인근 조회 결과 - 반환(size)={}, 요청(limit)={}, baseKm={} (+보정 {}), cap={}",
			out.size(), limit, km, CENTROID_BONUS_KM, MAX_RADIUS_CAP_KM);
		return out;
	}

	/**
	 * 특정 멤버(centerMemberId)를 중심으로 인근 멤버를 조회한다.
	 * <p>요청 반경을 기준으로 오차 보정(+2km) 후, 단계적으로 반경을 확장하여
	 * 최소 후보 수를 확보한다. 결과는 거리 오름차순으로 최대 {@code limit}명 반환하며,
	 * 중심 멤버 자신은 결과에서 제외한다.</p>
	 *
	 * @param centerMemberId 중심 멤버 ID
	 * @param km             기본 반경(km)
	 * @param limit          최대 반환 개수
	 * @return 가까운 순의 멤버 응답 리스트(0~limit)
	 */
	@Transactional(readOnly = true)
	@Override
	public List<NearbyMemberResponse> findNearbyOf(long centerMemberId, int km, int limit) {
		log.info("[MEMBER][LOCATION] 인근 멤버 조회 요청(멤버 기준) - centerId={}, km={}, limit={}", centerMemberId, km, limit);

		GeoIndexPort geoIndex = geoIndexProvider.getIfAvailable();
		if (geoIndex == null) return List.of();

		List<GeoIndexPort.GeoHit> hits = progressiveSearchByMember(
			geoIndex, centerMemberId, km, limit, MIN_CANDIDATES
		);
		if (hits.isEmpty()) {
			log.info("[MEMBER][LOCATION] 인근 조회 결과(멤버 기준) - centerId={}, 후보 없음", centerMemberId);
			return List.of();
		}

		// 자기 자신 제외
		hits.removeIf(h -> Objects.equals(h.memberId(), centerMemberId));

		List<NearbyMemberResponse> out = buildNearbyDtos(hits, limit);
		log.info("[MEMBER][LOCATION] 인근 조회 결과(멤버 기준) - centerId={}, 반환(size)={}, 요청(limit)={}", centerMemberId, out.size(), limit);
		return out;
	}

	// 내부 유틸: 점진 확대 검색(좌표 기준)
	private List<GeoIndexPort.GeoHit> progressiveSearchByPoint(
		GeoIndexPort geoIndex, double lat, double lng, int baseKm, int limit, int minCandidates
	) {
		final int base = Math.min(baseKm + CENTROID_BONUS_KM, MAX_RADIUS_CAP_KM);
		final int fetch = Math.max(limit * 3, minCandidates * 3);
		final Map<Long, GeoIndexPort.GeoHit> merged = new LinkedHashMap<>();

		log.debug("[MEMBER][LOCATION] 확장 검색(좌표) 시작 - baseKm={} (+보정 {}), cap={}, step={}",
			baseKm, CENTROID_BONUS_KM, MAX_RADIUS_CAP_KM, EXPAND_OFFSETS_KM.length);

		for (int off : EXPAND_OFFSETS_KM) {
			int r = Math.min(base + off, MAX_RADIUS_CAP_KM);
			var stepHits = geoIndex.search(lat, lng, r * 1000.0, fetch);
			for (var h : stepHits) merged.putIfAbsent(h.memberId(), h);
			log.debug("[MEMBER][LOCATION] 확장 검색(좌표) 단계 - step=+{}km, 반경(km)={}, 누적후보(size)={}", off, r, merged.size());

			// 최소 확보 인원 달성 시 조기 종료
			if (merged.size() >= Math.min(limit, minCandidates)) break;
		}

		// 거리 오름차순 정렬 후 반환
		var list = new ArrayList<>(merged.values());
		list.sort(Comparator.comparingDouble(GeoIndexPort.GeoHit::distanceKm));
		return list;
	}

	// 내부 유틸: 점진 확대 검색(멤버 기준)
	private List<GeoIndexPort.GeoHit> progressiveSearchByMember(
		GeoIndexPort geoIndex, long centerMemberId, int baseKm, int limit, int minCandidates
	) {
		final int base = Math.min(baseKm + CENTROID_BONUS_KM, MAX_RADIUS_CAP_KM);
		final int fetch = Math.max(limit * 3, minCandidates * 3);
		final Map<Long, GeoIndexPort.GeoHit> merged = new LinkedHashMap<>();

		log.debug("[MEMBER][LOCATION] 확장 검색(멤버 기준) 시작 - centerId={}, baseKm={} (+보정 {}), cap={}, step={}",
			centerMemberId, baseKm, CENTROID_BONUS_KM, MAX_RADIUS_CAP_KM, EXPAND_OFFSETS_KM.length);

		for (int off : EXPAND_OFFSETS_KM) {
			int r = Math.min(base + off, MAX_RADIUS_CAP_KM);
			var stepHits = geoIndex.searchByMember(centerMemberId, r * 1000.0, fetch);
			for (var h : stepHits) merged.putIfAbsent(h.memberId(), h);
			log.debug("[MEMBER][LOCATION] 확장 검색(멤버 기준) 단계 - centerId={}, step=+{}km, 반경(km)={}, 누적후보(size)={}",
				centerMemberId, off, r, merged.size());

			if (merged.size() >= Math.min(limit, minCandidates)) break;
		}

		var list = new ArrayList<>(merged.values());
		list.sort(Comparator.comparingDouble(GeoIndexPort.GeoHit::distanceKm));
		return list;
	}

	// 내부 유틸: 배치 조회 + 거리 반올림 + DTO 변환
	private List<NearbyMemberResponse> buildNearbyDtos(List<GeoIndexPort.GeoHit> hits, int limit) {
		List<Long> ids = hits.stream().map(GeoIndexPort.GeoHit::memberId).toList();

		// memberId -> distance(km)
		Map<Long, Double> distMap = hits.stream()
			.collect(Collectors.toMap(
				GeoIndexPort.GeoHit::memberId,
				GeoIndexPort.GeoHit::distanceKm,
				(a, b) -> a
			));

		// 한 번에 배치 조회
		List<Member> fetched = memberRepository.findAllByIdIn(ids);

		// 빠른 접근 위해 map 구성
		Map<Long, Member> byId = fetched.stream()
			.collect(Collectors.toMap(Member::getMemberId, m -> m));

		// ids(=거리순) 그대로 돌면서 필터링 + DTO 변환
		List<NearbyMemberResponse> out = new ArrayList<>();
		for (Long id : ids) {
			Member m = byId.get(id);
			if (m == null) continue;                       // DB에 없으면 스킵(삭제 등)
			if (m.isWithdrawn() || !m.isMatchingEnabled()) // 소프트 탈퇴/매칭 OFF 제외
				continue;

			Double d = distMap.get(id);
			out.add(new NearbyMemberResponse(
				m.getMemberId(),
				m.getMemberProfile().getNickname(),
				m.getAdditionalInfo().getAddress(),
				round2(d)
			));
			if (out.size() >= limit) break;
		}
		return out;
	}

	private static Double round2(Double v) {
		if (v == null) return null;
		return Math.round(v * 100.0) / 100.0;
	}
}