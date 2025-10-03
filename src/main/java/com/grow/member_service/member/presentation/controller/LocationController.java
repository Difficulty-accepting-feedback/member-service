package com.grow.member_service.member.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.global.dto.RsData;
import com.grow.member_service.member.application.dto.NearbyMemberResponse;
import com.grow.member_service.member.application.service.LocationApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "Location", description = "위치/매칭 API")
public class LocationController {

	private final LocationApplicationService locationSvc;

	@Operation(
		summary = "내 지역 저장 및 인덱싱",
		description = """
			시/군/구 라벨(region)과 시군구코드(sggCode)로 홈 좌표를 인덱싱
			- sggCode가 존재하면 코드 기반 매핑을 우선 사용
			- 저장 후 Redis GEO(geo:home)에 업서트되어 인근 조회에 즉시 반영
			"""
	)
	@PutMapping("/me/region")
	public ResponseEntity<RsData<Void>> updateRegion(
		@Parameter(hidden = true) @RequestHeader("X-Authorization-Id") Long memberId,
		@RequestBody @Valid RegionReq req
	) {
		locationSvc.updateMyRegion(memberId, req.region(), req.sggCode());
		return ResponseEntity.ok(new RsData<>("200", "지역 업데이트 성공", null));
	}

	@Operation(
		summary = "인근 멤버 조회(좌표 기준)",
		description = """
			주어진 위/경도(lat,lng)를 중심으로 가까운 순으로 최대 limit명 반환
			내부적으로 반경 점진 확대(base -> +5 → +10 → +15, cap=30km)하여 최소 후보 확보
			응답은 거리 오름차순, 거리 포함
			"""
	)
	@GetMapping("/nearby")
	public ResponseEntity<RsData<List<NearbyMemberResponse>>> nearby(
		@Parameter(description = "위도", example = "37.498084")
		@RequestParam double lat,
		@Parameter(description = "경도", example = "127.027610")
		@RequestParam double lng,
		@Parameter(description = "기본 반경(km). 도시 구 기준 7km 권장", example = "7")
		@RequestParam(defaultValue = "7") int km,
		@Parameter(description = "최대 반환 개수", example = "20")
		@RequestParam(defaultValue = "20") int limit
	) {
		List<NearbyMemberResponse> data = locationSvc.findNearby(lat, lng, km, limit);
		return ResponseEntity.ok(new RsData<>("200", "인근 조회(좌표) 성공", data));
	}

	@Operation(
		summary = "인근 멤버 조회(멤버 기준)",
		description = """
			멤버 ID를 중심으로 가까운 순으로 최대 limit명 반환
			내부적으로 반경 점진 확대(base -> +5 → +10 → +15, cap=30km)하여 최소 후보 확보
			응답은 거리 오름차순, 거리 포함
			"""
	)
	@GetMapping("/{memberId}/nearby")
	public ResponseEntity<RsData<List<NearbyMemberResponse>>> nearbyOf(
		@Parameter(description = "중심 멤버 ID", example = "1")
		@PathVariable long memberId,
		@Parameter(description = "기본 반경(km). 도시 구 기준 7km 권장", example = "7")
		@RequestParam(defaultValue = "7") int km,
		@Parameter(description = "최대 반환 개수", example = "20")
		@RequestParam(defaultValue = "20") int limit
	) {
		List<NearbyMemberResponse> data = locationSvc.findNearbyOf(memberId, km, limit);
		return ResponseEntity.ok(new RsData<>("200", "인근 조회(멤버) 성공", data));
	}

	public record RegionReq(
		@Schema(description = "시/군/구 라벨(예: 서울특별시 강남구)", example = "서울특별시 강남구")
		String region,
		@Schema(description = "행정구역 시군구코드", example = "11680")
		String sggCode
	) {}
}