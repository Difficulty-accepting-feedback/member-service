package com.grow.member_service.member.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grow.member_service.global.exception.ServiceException;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.model.MemberProfile;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.presentation.controller.MemberInfoController.MemberInfo;

@ExtendWith(MockitoExtension.class)
class MemberNameServiceImplTest {

	@Mock private MemberRepository memberRepository;
	@Mock private Member member;
	@Mock private MemberProfile memberProfile;

	@InjectMocks private MemberNameServiceImpl memberNameService;

	@Nested
	@DisplayName("findMemberNameById")
	class FindMemberNameById {

		@Test
		@DisplayName("정상 조회")
		void success_find_nickname_by_memberId() {
			Long memberId = 1L;
			String nickname = "사람";

			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			given(member.getMemberProfile()).willReturn(memberProfile);
			given(memberProfile.getNickname()).willReturn(nickname);

			String result = memberNameService.findMemberNameById(memberId);

			assertEquals(nickname, result);
			then(memberRepository).should().findById(memberId);
			// getMemberProfile은 로깅 + 반환용 2번 호출됨
			then(member).should(atLeastOnce()).getMemberProfile();
			then(memberProfile).should(atLeastOnce()).getNickname();
		}

		@Test
		@DisplayName("멤버 없으면 예외 발생")
		void fail_when_member_not_found() {
			Long notExistId = 999L;
			given(memberRepository.findById(notExistId)).willReturn(Optional.empty());

			assertThrows(ServiceException.class,
				() -> memberNameService.findMemberNameById(notExistId));
		}

		@Test
		@DisplayName("MemberProfile이 null이면 NPE 발생")
		void edge_null_profile_throws_npe() {
			Long memberId = 2L;
			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			given(member.getMemberProfile()).willReturn(null);

			assertThrows(NullPointerException.class,
				() -> memberNameService.findMemberNameById(memberId));
		}
	}

	@Nested
	@DisplayName("findMemberInfoById")
	class FindMemberInfoById {

		@Test
		@DisplayName("정상 조회")
		void success_find_member_info_by_memberId() {
			Long memberId = 3L;
			Double score = 1200.0;
			String nickname = "홍길동";

			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			given(member.getScore()).willReturn(score);
			given(member.getMemberProfile()).willReturn(memberProfile);
			given(memberProfile.getNickname()).willReturn(nickname);

			MemberInfo result = memberNameService.findMemberInfoById(memberId);

			assertNotNull(result);
			assertEquals(score, result.getScore());
			assertEquals(nickname, result.getNickname());

			// 호출 횟수는 로깅 포함 → 최소 2번 이상이 될 수 있으니 atLeastOnce로 검증
			then(member).should(atLeastOnce()).getMemberProfile();
			then(memberProfile).should(atLeastOnce()).getNickname();
			then(member).should(atLeastOnce()).getScore();
		}

		@Test
		@DisplayName("멤버 없으면 예외 발생")
		void fail_when_member_not_found() {
			Long notExistId = 404L;
			given(memberRepository.findById(notExistId)).willReturn(Optional.empty());

			assertThrows(ServiceException.class,
				() -> memberNameService.findMemberInfoById(notExistId));
		}

		@Test
		@DisplayName("MemberProfile이 null이면 NPE 발생")
		void edge_null_profile_throws_npe() {
			Long memberId = 5L;
			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			given(member.getMemberProfile()).willReturn(null);

			assertThrows(NullPointerException.class,
				() -> memberNameService.findMemberInfoById(memberId));
		}
	}
}