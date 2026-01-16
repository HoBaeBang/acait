package com.aslan.academymanagement.config.auth;

import com.aslan.academymanagement.config.auth.dto.CustomUserDetails;
import com.aslan.academymanagement.config.auth.dto.OAuthAttributes;
import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.repository.AcademyRepository;
import com.aslan.academymanagement.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final AcademyRepository academyRepository;
    private static final String SUPER_ADMIN_EMAIL = "aslanhobae@gmail.com";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        Member member;

        // 슈퍼 계정 체크 및 자동 생성/업데이트
        if (SUPER_ADMIN_EMAIL.equals(attributes.getEmail())) {
            member = processSuperAdmin(attributes);
        } else {
            // 일반 사용자: DB 조회 (자동 저장 X)
            Optional<Member> memberOptional = memberRepository.findByGoogleEmail(attributes.getEmail());
            
            // 미가입 사용자는 임시 Member 객체 생성 (DB 저장 안 함)
            member = memberOptional.orElse(Member.builder()
                    .googleEmail(attributes.getEmail())
                    .name(attributes.getName())
                    .picture(attributes.getPicture())
                    .role(Role.ROLE_GUEST) // Role.GUEST -> Role.ROLE_GUEST 수정
                    .build());
        }

        return new CustomUserDetails(member, oAuth2User.getAttributes());
    }

    private Member processSuperAdmin(OAuthAttributes attributes) {
        log.info("👑 슈퍼 관리자 로그인 감지: {}", attributes.getEmail());

        Member member = memberRepository.findByGoogleEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElseGet(() -> createSuperAdmin(attributes));

        // 슈퍼 계정 권한/상태 강제 업데이트 (필요 시)
        if (member.getRole() != Role.ROLE_ADMIN || member.getStatus() != MemberStatus.ACTIVE) {
             // 엔티티 비즈니스 메서드로 업데이트 권장
        }
        
        return memberRepository.save(member);
    }

    private Member createSuperAdmin(OAuthAttributes attributes) {
        Academy academy = academyRepository.findAll().stream().findFirst()
                .orElseGet(() -> academyRepository.save(new Academy("ACAIT 본사")));

        return Member.builder()
                .academy(academy)
                .googleEmail(attributes.getEmail())
                .name(attributes.getName())
                .picture(attributes.getPicture())
                .role(Role.ROLE_ADMIN)
                .status(MemberStatus.ACTIVE)
                .provider(attributes.getProvider())
                .providerId(attributes.getProviderId())
                .phone("010-0000-0000")
                .contactEmail(attributes.getEmail())
                .build();
    }
}
