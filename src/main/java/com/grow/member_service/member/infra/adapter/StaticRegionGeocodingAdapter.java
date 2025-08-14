package com.grow.member_service.member.infra.adapter;

import org.springframework.stereotype.Component;

import com.grow.member_service.member.application.port.GeocodingPort;
import com.grow.member_service.member.infra.region.RegionCenterResolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StaticRegionGeocodingAdapter implements GeocodingPort {

	private final RegionCenterResolver resolver;

	/**
	 * 정적 지역 매핑을 사용하여 지역 이름을 좌표로 변환합니다.
	 */
	@Override
	public LatLng geocodeRegion(String regionText) {
		var e = resolver.resolve(regionText);
		if (e == null) throw new IllegalArgumentException("알 수 없는 지역: " + regionText);
		return new LatLng(e.lat(), e.lon());
	}
}