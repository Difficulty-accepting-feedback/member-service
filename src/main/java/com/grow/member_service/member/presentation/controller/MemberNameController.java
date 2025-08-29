package com.grow.member_service.member.presentation.controller;

import com.grow.member_service.member.application.service.MemberNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/members")
@RequiredArgsConstructor
public class MemberNameController {

    private final MemberNameService memberNameService;

    @GetMapping("{memberId}")
    private ResponseEntity<String> getMemberName(@PathVariable("memberId") Long memberId) {
        String memberName = memberNameService.findMemberNameById(memberId);
        return ResponseEntity.ok(memberName);
    }
}
