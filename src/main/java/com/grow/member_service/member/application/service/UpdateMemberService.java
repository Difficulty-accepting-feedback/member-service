package com.grow.member_service.member.application.service;

import com.grow.member_service.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMemberService {

    private final MemberRepository memberRepository;
}
