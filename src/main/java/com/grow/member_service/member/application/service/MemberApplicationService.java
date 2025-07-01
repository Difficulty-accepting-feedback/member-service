package com.grow.member_service.member.application.service;

import com.grow.member_service.member.domain.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class MemberApplicationService {

    private final MemberRepository memberRepository;
}
