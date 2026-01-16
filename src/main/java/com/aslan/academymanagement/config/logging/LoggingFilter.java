package com.aslan.academymanagement.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

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

            // Request Header 추출
            Map<String, String> requestHeaders = new HashMap<>();
            Enumeration<String> headerNames = requestWrapper.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                requestHeaders.put(headerName, requestWrapper.getHeader(headerName));
            }

            // Response Header 추출
            Map<String, String> responseHeaders = new HashMap<>();
            for (String headerName : responseWrapper.getHeaderNames()) {
                responseHeaders.put(headerName, responseWrapper.getHeader(headerName));
            }

            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

            log.info("👉 [REQUEST] {} {} | Headers: {} | Body: {}", 
                    request.getMethod(), request.getRequestURI(), requestHeaders, requestBody);

            log.info("👈 [RESPONSE] {} {} | Status: {} | Duration: {}ms | Headers: {} | Body: {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, responseHeaders, responseBody);

            responseWrapper.copyBodyToResponse();
        }
    }
}
