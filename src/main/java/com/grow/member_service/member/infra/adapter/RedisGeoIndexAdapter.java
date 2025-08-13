package com.grow.member_service.member.infra.adapter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.grow.member_service.member.application.port.GeoIndexPort;

import lombok.RequiredArgsConstructor;

/**
 * Redis GEO 어댑터
 * - 키: geo:home
 * - 저장: upsert(long, double, double) 에서 (memberId -> (lon, lat)) 업서트
 * - 조회: {search(double, double, double, int) / searchByMember(long, double, int)
 *   - Redis WITHDIST 옵션으로 거리(km)를 수집 -> GeoHit로 변환
 *   - 결과는 거리 오름차순
 */
@Component
@RequiredArgsConstructor
public class RedisGeoIndexAdapter implements GeoIndexPort {

	private static final String KEY = "geo:home"; // 홈(프로필) 전용 인덱스 키
	private final StringRedisTemplate rt;

	/**
	 * 멤버의 홈 좌표를 GEO 인덱스에 업서트
	 *
	 * @param memberId 멤버 ID
	 * @param lat      위도(latitude)
	 * @param lng      경도(longitude)
	 */
	@Override
	public void upsert(long memberId, double lat, double lng) {
		// Redis Point는 (경도, 위도) 순서
		rt.opsForGeo().add(KEY, new Point(lng, lat), String.valueOf(memberId));
	}

	/**
	 * 주어진 좌표(lat,lng)를 중심으로 반경 내 멤버를 거리 오름차순으로 조회
	 * Redis WITHDIST로 거리(km)를 함께 반환
	 *
	 * @param lat           중심 위도
	 * @param lng           중심 경도
	 * @param radiusMeters  반경(미터)
	 * @param limit         최대 개수(1 이상)
	 * @return 거리 포함 히트 목록(가까운 순)
	 */
	@Override
	public List<GeoHit> search(double lat, double lng, double radiusMeters, int limit) {
		Point center = new Point(lng, lat);
		Distance distance = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);
		Circle circle = new Circle(center, distance);

		RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
			.newGeoRadiusArgs()
			.includeDistance() // WITHDIST: 거리 포함
			.sortAscending()   // 거리 오름차순
			.limit(Math.max(1, limit));

		GeoResults<RedisGeoCommands.GeoLocation<String>> results =
			rt.opsForGeo().radius(KEY, circle, args);

		if (results == null || results.getContent().isEmpty()) {
			return Collections.emptyList();
		}

		return results.getContent().stream()
			.map(r -> new GeoHit(
				Long.valueOf(r.getContent().getName()),
				// Spring Data Redis는 Distance를 km로 반환(위에서 Metrics.KILOMETERS 사용)
				r.getDistance() == null ? 0.0 : r.getDistance().getValue()
			))
			.collect(Collectors.toList());
	}

	/**
	 * 특정 멤버를 중심으로 반경 내 멤버를 거리 오름차순으로 조회
	 * Redis WITHDIST로 거리(km)를 함께 반환
	 *
	 * @param centerMemberId 중심 멤버 ID
	 * @param radiusMeters   반경(미터)
	 * @param limit          최대 개수(1 이상)
	 * @return 거리 포함 히트 목록(가까운 순)
	 */
	@Override
	public List<GeoHit> searchByMember(long centerMemberId, double radiusMeters, int limit) {
		Distance distance = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);

		RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
			.newGeoRadiusArgs()
			.includeDistance() // WITHDIST: 거리 포함
			.sortAscending()   // 거리 오름차순
			.limit(Math.max(1, limit));

		GeoResults<RedisGeoCommands.GeoLocation<String>> results =
			rt.opsForGeo().radius(KEY, String.valueOf(centerMemberId), distance, args);

		if (results == null || results.getContent().isEmpty()) {
			return Collections.emptyList();
		}

		return results.getContent().stream()
			.map(r -> new GeoHit(
				Long.valueOf(r.getContent().getName()),
				r.getDistance() == null ? 0.0 : r.getDistance().getValue()
			))
			.collect(Collectors.toList());
	}
}