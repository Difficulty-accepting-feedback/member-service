package com.grow.member_service.member.presentation.controller;

import com.grow.member_service.member.application.service.MemberNameService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberInfoController {

    private final MemberNameService memberNameService;

    @GetMapping("/api/v2/members/{memberId}")
    private ResponseEntity<String> getMemberName(@PathVariable("memberId") Long memberId) {
        String memberName = memberNameService.findMemberNameById(memberId);
        return ResponseEntity.ok(memberName);
    }

    @GetMapping("/api/v3/members/{memberId}")
    private ResponseEntity<MemberInfo> getMemberInfo(@PathVariable("memberId") Long memberId) {
        MemberInfo info = memberNameService.findMemberInfoById(memberId);
        return ResponseEntity.ok(info);
    }

    @Getter
    @AllArgsConstructor
    public static class MemberInfo {
        private final double score;
        private final String nickname;
    }
}
