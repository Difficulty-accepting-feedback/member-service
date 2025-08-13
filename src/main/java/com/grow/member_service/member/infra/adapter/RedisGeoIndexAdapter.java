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

@Component
@RequiredArgsConstructor
public class RedisGeoIndexAdapter implements GeoIndexPort {

	private static final String KEY = "geo:home"; // 홈(프로필) 전용 인덱스
	private final StringRedisTemplate rt;

	@Override
	public void upsert(long memberId, double lat, double lng) {
		// Redis Point는 (경도, 위도)
		rt.opsForGeo().add(KEY, new Point(lng, lat), String.valueOf(memberId));
	}

	@Override
	public List<GeoHit> search(double lat, double lng, double radiusMeters, int limit) {
		Point center = new Point(lng, lat);
		Distance dist = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);
		Circle circle = new Circle(center, dist);

		var args = RedisGeoCommands.GeoRadiusCommandArgs
			.newGeoRadiusArgs()
			.includeDistance() // WITHDIST
			.sortAscending()
			.limit(Math.max(1, limit));

		GeoResults<RedisGeoCommands.GeoLocation<String>> results =
			rt.opsForGeo().radius(KEY, circle, args);

		if (results == null || results.getContent().isEmpty()) return Collections.emptyList();

		return results.getContent().stream()
			.map(r -> new GeoHit(
				Long.valueOf(r.getContent().getName()),
				r.getDistance() == null ? 0.0 : r.getDistance().getValue() // km
			))
			.collect(Collectors.toList());
	}

	@Override
	public List<GeoHit> searchByMember(long centerMemberId, double radiusMeters, int limit) {
		Distance dist = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);

		var args = RedisGeoCommands.GeoRadiusCommandArgs
			.newGeoRadiusArgs()
			.includeDistance() // WITHDIST
			.sortAscending()
			.limit(Math.max(1, limit));

		GeoResults<RedisGeoCommands.GeoLocation<String>> results =
			rt.opsForGeo().radius(KEY, String.valueOf(centerMemberId), dist, args);

		if (results == null || results.getContent().isEmpty()) return Collections.emptyList();

		return results.getContent().stream()
			.map(r -> new GeoHit(
				Long.valueOf(r.getContent().getName()),
				r.getDistance() == null ? 0.0 : r.getDistance().getValue()
			))
			.collect(Collectors.toList());
	}
}