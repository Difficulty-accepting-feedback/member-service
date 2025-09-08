package com.grow.member_service.member.application.service;

import static com.grow.member_service.member.presentation.controller.MemberInfoController.*;

public interface MemberNameService {

    String findMemberNameById(Long memberId);

    MemberInfo findMemberInfoById(Long memberId);
}
