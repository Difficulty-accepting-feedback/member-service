package com.grow.member_service.auth.config;

import static org.springframework.security.config.Customizer.*;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.grow.member_service.auth.infra.security.jwt.JwtAuthenticationFilter;
import com.grow.member_service.auth.infra.security.oauth2.adapter.CustomOAuth2Service;
import com.grow.member_service.auth.infra.security.oauth2.handler.OAuth2AuthenticationFailureHandler;
import com.grow.member_service.auth.infra.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2Service oauth2Service;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final JwtAuthenticationFilter jwtFilter;

    // 게이트웨이 미구현으로 인한 임시 설정 (추후 제거)
    @Bean
    @Order(0)
    public SecurityFilterChain internalOpenSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/internal/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * API 요청: JWT 검증만 수행, OAuth2·폼 로그인은 비활성화
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/members/resolve").permitAll() // 인증 제외
                        .requestMatchers("/api/v2/**").permitAll()  // 인증 제외
                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o.disable())
                .formLogin(f -> f.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Web 요청: OAuth2 로그인 UI 활성화, 기타 퍼블릭 경로는 모두 permitAll
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/oauth2/**",
                                "/login/oauth2/code/**",
                                "/error",
                                "/h2-console/**",
                                "/health",
                                "/env",
                                "/v3/api-docs",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.userService(oauth2Service))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                );

        return http.build();
    }

    //게이트웨이 미구현 상태에서 cors 허용하기 위해 추가
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // 프론트 도메인
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS", "PUT"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept", "Authorization", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}