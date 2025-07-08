package com.grow.member_service.auth.infra.security.jwt;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 *  Access/Refresh 토큰 처리 + 인증 필터
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest req,
		HttpServletResponse res,
		FilterChain chain)
		throws ServletException, IOException {

		Optional<String> accessOpt = getCookieValue(req, "access_token");

		if (accessOpt.isPresent()) {
			try {
				String access = accessOpt.get();
				if (tokenProvider.validateToken(access)) {
					authenticate(tokenProvider.getMemberId(access));
				}
			} catch (ExpiredJwtException expired) {
				// 2) 만약 액세스 만료라면 refresh 로 재발급 시도
				Optional<String> refreshOpt = getCookieValue(req, "refresh_token");
				if (refreshOpt.isPresent() && tokenProvider.validateToken(refreshOpt.get())) {
					Long memberId = tokenProvider.getMemberId(refreshOpt.get());
					// 새 토큰 생성
					String newAccess  = tokenProvider.createAccessToken(memberId);
					String newRefresh = tokenProvider.createRefreshToken(memberId);
					// 쿠키 재설정
					ResponseCookie aCookie = ResponseCookie.from("access_token", newAccess)
						.httpOnly(true).secure(true).path("/").maxAge(Duration.ofMinutes(30)).sameSite("Strict").build();
					ResponseCookie rCookie = ResponseCookie.from("refresh_token", newRefresh)
						.httpOnly(true).secure(true).path("/").maxAge(Duration.ofDays(7)).sameSite("Strict").build();
					res.addHeader(HttpHeaders.SET_COOKIE, aCookie.toString());
					res.addHeader(HttpHeaders.SET_COOKIE, rCookie.toString());
					authenticate(memberId);
				}
			}
		}

		chain.doFilter(req, res);
	}

	/** SecurityContextHolder에 인증 정보 등록 */
	private void authenticate(Long memberId) {
		var auth = new UsernamePasswordAuthenticationToken(
			memberId, null, Collections.emptyList()
		);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	/** 요청 쿠키에서 특정 이름의 값을 찾기 */
	private Optional<String> getCookieValue(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) return Optional.empty();
		return Arrays.stream(cookies)
			.filter(c -> name.equals(c.getName()))
			.map(Cookie::getValue)
			.findFirst();
	}
}