package com.aslan.academymanagement.config.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private static final String[] EXCLUDE_PATHS = {
            "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars",
            "/css", "/js", "/images", "/favicon.ico",
            "/h2-console"
    };

    // 로깅에서 제외할 불필요한 헤더 목록 (소문자로 비교)
    private static final Set<String> IGNORE_HEADERS = Set.of(
            "sec-ch-ua", "sec-ch-ua-mobile", "sec-ch-ua-platform",
            "upgrade-insecure-requests", "user-agent", "accept", "accept-encoding", "accept-language",
            "connection", "host", "content-length", "pragma", "cache-control",
            "sec-fetch-site", "sec-fetch-mode", "sec-fetch-user", "sec-fetch-dest",
            "referer", "origin", "cookie", "x-content-type-options", "x-xss-protection", "x-frame-options",
            "vary", "transfer-encoding", "keep-alive", "date"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return Arrays.stream(EXCLUDE_PATHS).anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper);
            logResponse(responseWrapper, requestWrapper.getMethod(), requestWrapper.getRequestURI(), duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String headers = getHeaders(request);
        String body = getBody(request.getContentAsByteArray());
        String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();

        log.info("\n" +
                "==================== [REQUEST] ====================\n" +
                "URI      : {} {}{}\n" +
                "Headers  : {}\n" +
                "Body     : {}\n" +
                "===================================================",
                request.getMethod(), request.getRequestURI(), queryString, headers, body);
    }

    private void logResponse(ContentCachingResponseWrapper response, String method, String uri, long duration) {
        String headers = getHeaders(response);
        String body = getBody(response.getContentAsByteArray());

        log.info("\n" +
                "==================== [RESPONSE] ===================\n" +
                "URI      : {} {} ({}ms)\n" +
                "Status   : {}\n" +
                "Headers  : {}\n" +
                "Body     : {}\n" +
                "===================================================",
                method, uri, duration, response.getStatus(), headers, body);
    }

    private String getHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!IGNORE_HEADERS.contains(headerName.toLowerCase())) {
                headerMap.put(headerName, request.getHeader(headerName));
            }
        }
        return headerMap.isEmpty() ? "(empty)" : headerMap.toString();
    }

    private String getHeaders(HttpServletResponse response) {
        Map<String, String> headerMap = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            if (!IGNORE_HEADERS.contains(headerName.toLowerCase())) {
                headerMap.put(headerName, response.getHeader(headerName));
            }
        }
        return headerMap.isEmpty() ? "(empty)" : headerMap.toString();
    }

    private String getBody(byte[] content) {
        if (content.length == 0) {
            return "(empty)";
        }
        try {
            String contentString = new String(content, StandardCharsets.UTF_8);
            // JSON 포맷팅 시도
            Object json = objectMapper.readValue(contentString, Object.class);
            return "\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            // JSON이 아니면 그냥 문자열로 반환
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
