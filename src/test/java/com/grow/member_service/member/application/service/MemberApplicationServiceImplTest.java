package com.grow.member_service.member.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.grow.member_service.achievement.trigger.listener.AchievementTriggerProducer;
import com.grow.member_service.common.exception.MemberException;
import com.grow.member_service.member.application.dto.MemberInfoResponse;
import com.grow.member_service.member.application.port.GeoIndexPort;
import com.grow.member_service.member.application.service.impl.MemberApplicationServiceImpl;
import com.grow.member_service.member.domain.exception.MemberDomainException;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.enums.Platform;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.domain.repository.MemberWithdrawalLogRepository;
import com.grow.member_service.member.domain.service.MemberService;
import com.grow.member_service.member.presentation.dto.MemberUpdateRequest;


@ExtendWith(MockitoExtension.class)
class MemberApplicationServiceImplTest {

	@Mock private MemberRepository memberRepository;
	@Mock private MemberService memberService;
	@Mock private MemberWithdrawalLogRepository withdrawalLogRepository;

	@Mock ObjectProvider<GeoIndexPort> geoIndexProvider;
	@Mock GeoIndexPort geoIndexPort;

	@Mock private KafkaTemplate<String, String> kafkaTemplate;

	@Mock private StringRedisTemplate redis;
	@Mock private ValueOperations<String, String> valueOps;

	@Mock private AchievementTriggerProducer achievementTriggerProducer;

	@InjectMocks
	private MemberApplicationServiceImpl service;

	private Member sampleMember;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(
			Instant.parse("2025-01-01T00:00:00Z"),
			ZoneOffset.UTC
		);
		sampleMember = new Member(
			new com.grow.member_service.member.domain.model.MemberProfile(
				"user@example.com",
				"origNick",
				"http://example.com/orig.png",
				Platform.KAKAO,
				"ext-id"
			),
			new com.grow.member_service.member.domain.model.MemberAdditionalInfo(
				"01012345678",
				"Seoul"
			),
			fixedClock
		);
	}

	// ✅ Member 도메인 객체에 id를 심어주는 헬퍼
	private void setId(Member member, Long id) {
		ReflectionTestUtils.setField(member, "memberId", id);
	}

	@Test
	@DisplayName("getMyInfo(): 회원이 존재하면 MemberInfoResponse 반환")
	void getMyInfo_Found_ReturnsResponse() {
		Long memberId = 1L;
		setId(sampleMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));

		MemberInfoResponse resp = service.getMyInfo(memberId);

		assertNotNull(resp, "MemberInfoResponse가 반환되어야 한다");
		verify(memberRepository).findById(memberId);
	}

	@Test
	@DisplayName("getMyInfo(): 회원이 존재하지 않으면 MemberException 발생")
	void getMyInfo_NotFound_ThrowsMemberException() {
		when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(MemberException.class, () -> service.getMyInfo(99L));
	}

	@Test
	@DisplayName("withdraw(): 회원이 존재하면 로그 저장 → 도메인 탈퇴 → 저장 + GEO 제거")
	void withdraw_Found_LogsAndWithdraws_AndRemoveFromGeo() {
		Long memberId = 2L;
		setId(sampleMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));
		when(geoIndexProvider.getIfAvailable()).thenReturn(geoIndexPort);

		service.withdraw(memberId);

		InOrder inOrder = inOrder(withdrawalLogRepository, memberRepository);
		inOrder.verify(withdrawalLogRepository).saveFromMember(sampleMember);
		assertTrue(sampleMember.isWithdrawn());
		inOrder.verify(memberRepository).save(sampleMember);

		verify(geoIndexPort).remove(memberId);
	}

	@Test
	@DisplayName("withdraw(): 회원이 존재하지 않으면 MemberException 발생")
	void withdraw_NotFound_ThrowsMemberException() {
		when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(MemberException.class, () -> service.withdraw(123L));
		verifyNoInteractions(withdrawalLogRepository);
		verify(memberRepository, never()).save(any());
	}

	@Test
	@DisplayName("updateMember(): 회원이 존재하지 않으면 MemberException 발생")
	void updateMember_NotFound_ThrowsMemberException() {
		when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

		MemberUpdateRequest req = new MemberUpdateRequest("nick", "img", "addr");
		assertThrows(MemberException.class, () -> service.updateMember(5L, req));
	}

	@Test
	@DisplayName("updateMember(): 모든 필드가 주어지면 각 변경 메서드 호출 후 저장 + 주소 변경 시 업적 트리거 발행")
	void updateMember_AllFields_UpdatesAndSaves_AndPublishesTriggerOnAddressChange() {
		Long memberId = 3L;
		setId(sampleMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));
		when(memberService.isNicknameUnique("newNick")).thenReturn(true);

		MemberUpdateRequest req = new MemberUpdateRequest("newNick", "newImg", "newAddr");
		service.updateMember(memberId, req);

		assertEquals("newNick", sampleMember.getMemberProfile().getNickname());
		assertEquals("newImg", sampleMember.getMemberProfile().getProfileImage());
		assertEquals("newAddr", sampleMember.getAdditionalInfo().getAddress());
		verify(memberRepository).save(sampleMember);

		// ✅ 주소가 바뀌었으면 업적 트리거 발행 호출됨
		verify(achievementTriggerProducer, times(1)).send(any());
	}

	@Test
	@DisplayName("updateMember(): nickname 중복 시 MemberDomainException 발생")
	void updateMember_DuplicateNickname_ThrowsDomainException() {
		Long memberId = 4L;
		setId(sampleMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));
		when(memberService.isNicknameUnique("dupNick")).thenReturn(false);

		MemberUpdateRequest req = new MemberUpdateRequest("dupNick", "img", null);

		assertThrows(MemberDomainException.class, () -> service.updateMember(memberId, req));
		verify(memberRepository, never()).save(any());
		// 닉네임 충돌로 저장 전에 예외 → 트리거도 발행되지 않아야 함
		verify(achievementTriggerProducer, never()).send(any());
	}

	@Test
	@DisplayName("updateMember(): nickname null 이면 changeNickname 미호출(주소 변경 없으면 트리거도 없음)")
	void updateMember_NullNickname_SkipsNickname() {
		Long memberId = 5L;
		setId(sampleMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));

		MemberUpdateRequest req = new MemberUpdateRequest(null, "imgOnly", null);
		service.updateMember(memberId, req);

		assertEquals("imgOnly", sampleMember.getMemberProfile().getProfileImage());
		verify(memberRepository).save(sampleMember);
		verify(achievementTriggerProducer, never()).send(any()); // 주소 변경이 없으니 트리거 없음
	}

	@Test
	@DisplayName("updateMember(): address null 이면 changeAddress 미호출(트리거 없음)")
	void updateMember_NullAddress_SkipsAddress() {
		Long memberId = 6L;
		setId(sampleMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));
		when(memberService.isNicknameUnique("okNick")).thenReturn(true);

		MemberUpdateRequest req = new MemberUpdateRequest("okNick", "img", null);
		service.updateMember(memberId, req);

		assertEquals("okNick", sampleMember.getMemberProfile().getNickname());
		assertEquals("img", sampleMember.getMemberProfile().getProfileImage());
		assertEquals("Seoul", sampleMember.getAdditionalInfo().getAddress());
		verify(memberRepository).save(sampleMember);
		verify(achievementTriggerProducer, never()).send(any()); // 주소 변경이 없으니 트리거 없음
	}

	@Test
	@DisplayName("toggleMatching(): 회원이 존재하고 enabled=true 요청 시 매칭 기능 활성화")
	void toggleMatching_Enable_Success() {
		Long memberId = 7L;
		setId(sampleMember, memberId);

		sampleMember.disableMatching();
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));

		service.toggleMatching(memberId, true);

		assertTrue(sampleMember.isMatchingEnabled());
		verify(memberRepository).save(sampleMember);
	}

	@Test
	@DisplayName("toggleMatching(): 회원이 존재하고 enabled=false 요청 시 매칭 기능 비활성화")
	void toggleMatching_Disable_Success() {
		Long memberId = 8L;
		setId(sampleMember, memberId);

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));

		service.toggleMatching(memberId, false);

		assertFalse(sampleMember.isMatchingEnabled());
		verify(memberRepository).save(sampleMember);
	}

	@Test
	@DisplayName("toggleMatching(): 회원이 존재하지 않으면 MemberException 발생")
	void toggleMatching_NotFound_ThrowsMemberException() {
		when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(MemberException.class, () -> service.toggleMatching(9L, true));
		verify(memberRepository, never()).save(any());
	}
}