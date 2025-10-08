package com.grow.member_service.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행되도록 설정
@Component
public class MicroserviceMDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // 게이트웨이로부터 전달된 헤더 읽기
        String traceId = httpRequest.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString(); // 헤더 없으면 새로 생성
        }

        String clientIp = httpRequest.getHeader("X-Client-Ip");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = getClientIp(httpRequest); // 로컬 IP 추출
        }

        String requestUri = httpRequest.getHeader("X-Request-Uri");
        if (requestUri == null || requestUri.isEmpty()) {
            requestUri = httpRequest.getRequestURI(); // 로컬 URI 사용
        }

        // 응답 헤더에도 추가 (추적을 위해)
        httpResponse.addHeader("X-Trace-Id", traceId);
        httpResponse.addHeader("X-Client-Ip", clientIp);
        httpResponse.addHeader("X-Request-Uri", requestUri);

        // MDC에 값 삽입
        MDC.put("traceId", traceId);
        MDC.put("clientIp", clientIp);
        MDC.put("requestUri", requestUri);

        log.info("[MDC] traceId={}, clientIp={}, requestUri={}", traceId, clientIp, requestUri);

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // MDC 정리 (메모리 누수 방지)
            MDC.remove("traceId");
            MDC.remove("clientIp");
            MDC.remove("requestUri");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim(); // 프록시 고려
        }
        return request.getRemoteAddr();
    }
}