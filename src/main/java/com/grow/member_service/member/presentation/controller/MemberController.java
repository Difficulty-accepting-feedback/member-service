package com.grow.member_service.member.presentation.controller;

import com.grow.member_service.member.application.service.MemberApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@AllArgsConstructor
public class MemberController {

    private final MemberApplicationService memberApplicationService;
}
