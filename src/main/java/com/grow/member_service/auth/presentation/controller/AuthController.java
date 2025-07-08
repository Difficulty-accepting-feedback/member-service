package com.grow.member_service.auth.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grow.member_service.auth.application.dto.TokenResponse;
import com.grow.member_service.auth.application.service.OAuth2LoginService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final OAuth2LoginService loginService;

	/**
	 * OAuth2 로그인
	 */
	@GetMapping("/{provider}")
	public TokenResponse oauthLogin(
		@PathVariable String provider,
		@RequestParam String code
	) {
		return loginService.login(provider, code);
	}
}