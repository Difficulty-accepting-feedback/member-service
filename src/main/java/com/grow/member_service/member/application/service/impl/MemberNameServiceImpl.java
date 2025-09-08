package com.grow.member_service.member.application.service.impl;

import com.grow.member_service.global.exception.ErrorCode;
import com.grow.member_service.global.exception.ServiceException;
import com.grow.member_service.member.application.service.MemberNameService;
import com.grow.member_service.member.domain.model.Member;
import com.grow.member_service.member.domain.repository.MemberRepository;
import com.grow.member_service.member.presentation.controller.MemberInfoController;
import com.grow.member_service.member.presentation.controller.MemberInfoController.MemberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberNameServiceImpl implements MemberNameService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public String findMemberNameById(Long memberId) {
        log.info("[MEMBER][SEARCH][START] 회원 이름 조회 요청 시작 - memberId={}", memberId);

        Member findMember = memberRepository.findById(memberId).orElseThrow(
                () -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        log.info("[MEMBER][SEARCH][END] 회원 이름 조회 요청 완료 - memberId={}, nickname={}", memberId, findMember.getMemberProfile().getNickname());
        return findMember.getMemberProfile().getNickname();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberInfo findMemberInfoById(Long memberId) {
        log.info("[MEMBER][SEARCH][START] 회원 정보 조회 요청 시작 - memberId={}", memberId);

        Member findMember = memberRepository.findById(memberId).orElseThrow(
                () -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        log.info("[MEMBER][SEARCH][END] 회원 정보 조회 요청 완료 - nickname={}, score={}", findMember.getMemberProfile().getNickname(), findMember.getScore());
        return new MemberInfo(findMember.getScore(), findMember.getMemberProfile().getNickname()) ;
    }
}
