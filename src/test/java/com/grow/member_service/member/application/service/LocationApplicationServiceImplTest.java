package com.grow.member_service.member.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import com.grow.member_service.member.application.dto.NearbyMemberResponse;
import com.grow.member_service.member.application.port.GeoIndexPort;
import com.grow.member_service.member.application.port.GeocodingPort;
import com.grow.member_service.member.application.service.impl.LocationApplicationServiceImpl;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberAdditionalInfo;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.infra.region.RegionCenterResolver;

@ExtendWith(MockitoExtension.class)
class LocationApplicationServiceImplTest {

	@Mock MemberRepository memberRepository;
	@Mock ObjectProvider<GeocodingPort> geocodingProvider;
	@Mock ObjectProvider<GeoIndexPort>   geoIndexProvider;
	@Mock RegionCenterResolver regionResolver;

	@Mock GeocodingPort geocodingPort;
	@Mock GeoIndexPort geoIndex;

	LocationApplicationService sut;

	@BeforeEach
	void setUp() {
		// provider들이 올바른 타입을 반환하도록 스텁
		lenient().when(geocodingProvider.getIfAvailable()).thenReturn(geocodingPort);
		lenient().when(geoIndexProvider.getIfAvailable()).thenReturn(geoIndex);

		sut = new LocationApplicationServiceImpl(
			memberRepository, geocodingProvider, geoIndexProvider, regionResolver
		);
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// updateMyRegion
	// ─────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("updateMyRegion: sggCode 없이 텍스트 지오코딩 → GEO 업서트")
	void updateMyRegion_geocoding_success() {
		// given
		Long memberId = 1L;
		Member m = new Member(
			new MemberProfile("u@test.com", "user", null, Platform.KAKAO, "pid"),
			new MemberAdditionalInfo("01012345678", "서울특별시 서초구"),
			Clock.systemUTC()
		);
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(m));
		when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

		// 주소 공백 정규화 구현 여부에 의존하지 않도록 anyString()으로 스텁
		when(geocodingPort.geocodeRegion(anyString()))
			.thenReturn(new GeocodingPort.LatLng(37.4979, 127.0276));

		// when
		sut.updateMyRegion(memberId, "   서울특별시   강남구   ", null);

		// then
		verify(memberRepository).save(any(Member.class));
		verify(geoIndex).upsert(eq(memberId), eq(37.4979), eq(127.0276));
		verifyNoInteractions(regionResolver); // 코드 미사용 경로
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// findNearby (좌표 기준)
	// ─────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("findNearby: 점진 확장으로 후보 확보 → 거리 오름차순 + limit + 반올림")
	void findNearby_progressive_success() {
		// given
		double lat = 37.6, lng = 127.0;
		int km = 7, limit = 4;

		// step1(base): 2명
		List<GeoIndexPort.GeoHit> step1 = List.of(
			new GeoIndexPort.GeoHit(10L, 1.2345),
			new GeoIndexPort.GeoHit(20L, 2.0001)
		);
		// step2(base+5): 2명 추가
		List<GeoIndexPort.GeoHit> step2 = List.of(
			new GeoIndexPort.GeoHit(30L, 3.4999),
			new GeoIndexPort.GeoHit(40L, 4.0001)
		);

		when(geoIndex.search(eq(lat), eq(lng), anyDouble(), anyInt()))
			.thenReturn(step1)   // 1st call
			.thenReturn(step2);  // 2nd call

		// JPA 배치 조회: 서비스가 내부에서 정렬하므로 순서는 상관없음
		var m10 = deepMember(10L, "u10", "A");
		var m20 = deepMember(20L, "u20", "B");
		var m30 = deepMember(30L, "u30", "C");
		var m40 = deepMember(40L, "u40", "D");
		when(memberRepository.findAllByIdIn(anyList()))
			.thenReturn(List.of(m10, m20, m30, m40));

		// when
		List<NearbyMemberResponse> out = sut.findNearby(lat, lng, km, limit);

		// then
		assertThat(out).hasSize(4);
		assertThat(out.get(0).distanceKm()).isEqualTo(1.23);
		assertThat(out.get(1).distanceKm()).isEqualTo(2.0);
		assertThat(out.get(2).distanceKm()).isEqualTo(3.5);
		assertThat(out.get(3).distanceKm()).isEqualTo(4.0);

		verify(geoIndex, atLeast(2)).search(eq(lat), eq(lng), anyDouble(), anyInt());
		verify(memberRepository, times(1)).findAllByIdIn(anyList());
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// findNearbyOf (멤버 기준)
	// ─────────────────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("findNearbyOf: 중심 멤버 자신 제외 + 거리 오름차순")
	void findNearbyOf_excludes_self() {
		long centerId = 1L;
		int km = 7, limit = 3;

		List<GeoIndexPort.GeoHit> hits = List.of(
			new GeoIndexPort.GeoHit(centerId, 0.0), // self
			new GeoIndexPort.GeoHit(2L, 1.11),
			new GeoIndexPort.GeoHit(3L, 2.22)
		);
		when(geoIndex.searchByMember(eq(centerId), anyDouble(), anyInt()))
			.thenReturn(hits);

		var m2 = deepMember(2L, "u2", "R2");
		var m3 = deepMember(3L, "u3", "R3");
		when(memberRepository.findAllByIdIn(anyList()))
			.thenReturn(List.of(m2, m3));

		// when
		List<NearbyMemberResponse> out = sut.findNearbyOf(centerId, km, limit);

		// then
		assertThat(out).hasSize(2);
		assertThat(out).extracting(NearbyMemberResponse::memberId).containsExactly(2L, 3L);
		assertThat(out.get(0).distanceKm()).isEqualTo(1.11);
		assertThat(out.get(1).distanceKm()).isEqualTo(2.22);

		verify(geoIndex, times(1)).searchByMember(eq(centerId), anyDouble(), anyInt());
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// helpers
	// ─────────────────────────────────────────────────────────────────────────────

	private Member deepMember(Long id, String nick, String addr) {
		Member m = mock(Member.class, RETURNS_DEEP_STUBS);
		when(m.getMemberId()).thenReturn(id);
		when(m.getMemberProfile().getNickname()).thenReturn(nick);
		when(m.getAdditionalInfo().getAddress()).thenReturn(addr);
		// 필터 조건을 통과하도록 명시적으로 true/false 지정
		when(m.isWithdrawn()).thenReturn(false);
		when(m.isMatchingEnabled()).thenReturn(true);
		return m;
	}
}