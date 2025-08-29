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

@ExtendWith(MockitoExtension.class)
class MemberNameServiceImplTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private Member member;

	@Mock
	private MemberProfile memberProfile;

	@InjectMocks
	private MemberNameServiceImpl memberNameService;

	@Nested
	@DisplayName("findMemberNameById 동작")
	class FindMemberNameById {

		@Test
		@DisplayName("정상: memberId로 닉네임을 조회한다")
		void success_find_nickname_by_memberId() {
			// given
			Long memberId = 1L;
			String nickname = "사람";

			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			given(member.getMemberProfile()).willReturn(memberProfile);
			given(memberProfile.getNickname()).willReturn(nickname);

			// when
			String result = memberNameService.findMemberNameById(memberId);

			// then
			assertEquals(nickname, result);
			verify(memberRepository).findById(memberId);
		}

		@Test
		@DisplayName("실패: 멤버가 없으면 ServiceException 발생")
		void fail_when_member_not_found() {
			// given
			Long notExistId = 999L;
			given(memberRepository.findById(notExistId)).willReturn(Optional.empty());

			// when & then
			assertThrows(ServiceException.class,
				() -> memberNameService.findMemberNameById(notExistId));
			verify(memberRepository).findById(notExistId);
		}
	}
}