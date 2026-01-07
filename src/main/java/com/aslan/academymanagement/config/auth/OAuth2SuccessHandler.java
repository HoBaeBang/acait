package com.aslan.academymanagement.config.auth;

import com.aslan.academymanagement.config.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. JWT 토큰 생성
        String token = jwtTokenProvider.createToken(authentication);
        log.info(">>> JWT Token Generated: {}", token);

        // 2. 토큰을 담아 리다이렉트 (프론트엔드가 있다면 프론트 주소로)
        // 지금은 테스트를 위해 그냥 화면에 뿌려주거나, 특정 URL로 보냄
        String targetUrl = UriComponentsBuilder.fromUriString("/login-success") // 임시 URL
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
