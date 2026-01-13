package com.aslan.academymanagement.config.auth;

import com.aslan.academymanagement.config.auth.dto.OAuthAttributes;
import com.aslan.academymanagement.config.jwt.JwtTokenProvider;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 구글 이메일 추출 (CustomOAuth2UserService 로직 참고)
        // 주의: provider에 따라 속성 키가 다를 수 있음. 현재는 Google만 고려.
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        Optional<Member> memberOptional = memberRepository.findByGoogleEmail(email);

        // 1. 회원이 없는 경우 -> 회원가입 페이지로 리다이렉트
        if (memberOptional.isEmpty()) {
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/register")
                    .queryParam("googleEmail", email)
                    .queryParam("name", name)
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        Member member = memberOptional.get();

        // 2. 승인 대기 중인 경우 -> 대기 안내 페이지로 리다이렉트
        if (member.getStatus() == MemberStatus.PENDING) {
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/pending")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        // 3. 승인 거절된 경우 -> 거절 안내 페이지로 리다이렉트
        if (member.getStatus() == MemberStatus.REJECTED) {
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/rejected")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        // 4. 정상 회원 (ACTIVE) -> JWT 발급 및 로그인 성공 처리
        String token = jwtTokenProvider.createToken(authentication);
        log.info(">>> JWT Token Generated: {}", token);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login-success")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
