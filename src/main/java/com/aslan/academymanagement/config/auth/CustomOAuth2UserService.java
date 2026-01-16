package com.aslan.academymanagement.config.auth;

import com.aslan.academymanagement.config.auth.dto.OAuthAttributes;
import com.aslan.academymanagement.domain.Academy;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.MemberStatus;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.repository.AcademyRepository;
import com.aslan.academymanagement.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

        // 슈퍼 계정 체크 및 자동 생성/업데이트
        if (SUPER_ADMIN_EMAIL.equals(attributes.getEmail())) {
            return processSuperAdmin(attributes);
        }

        // 일반 사용자: DB 조회 (자동 저장 X)
        Optional<Member> memberOptional = memberRepository.findByGoogleEmail(attributes.getEmail());

        // 권한 설정: DB에 있으면 해당 Role, 없으면 GUEST (임시 권한)
        String roleKey = memberOptional.map(Member::getRoleKey).orElse("ROLE_GUEST");

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(roleKey)),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private OAuth2User processSuperAdmin(OAuthAttributes attributes) {
        log.info("👑 슈퍼 관리자 로그인 감지: {}", attributes.getEmail());

        Member member = memberRepository.findByGoogleEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElseGet(() -> createSuperAdmin(attributes));

        // 슈퍼 계정은 항상 ADMIN 권한과 ACTIVE 상태를 유지하도록 강제 업데이트
        if (member.getRole() != Role.ROLE_ADMIN || member.getStatus() != MemberStatus.ACTIVE) {
            // 엔티티에 setter가 없으므로, 빌더 패턴이나 별도 메서드로 업데이트해야 함.
            // 여기서는 편의상 save 시점에 덮어쓰거나, Member 엔티티에 updateRoleStatus 메서드 추가 권장.
            // 현재는 초기 생성 시점에만 적용됨. 이미 생성된 경우 DB에서 수동 변경 필요할 수 있음.
            // -> Member 엔티티에 비즈니스 메서드 추가하여 해결하는 것이 좋음.
        }
        
        memberRepository.save(member);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(Role.ROLE_ADMIN.getKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private Member createSuperAdmin(OAuthAttributes attributes) {
        // 슈퍼 관리자용 기본 학원 생성 (없으면)
        Academy academy = academyRepository.findAll().stream().findFirst()
                .orElseGet(() -> academyRepository.save(new Academy("ACAIT 본사")));

        return Member.builder()
                .academy(academy) // 필수 연관관계 주입
                .googleEmail(attributes.getEmail())
                .name(attributes.getName())
                .picture(attributes.getPicture())
                .role(Role.ROLE_ADMIN) // 관리자 권한 부여
                .status(MemberStatus.ACTIVE) // 즉시 활성화
                .provider(attributes.getProvider())
                .providerId(attributes.getProviderId())
                .phone("010-0000-0000") // 임시 값
                .contactEmail(attributes.getEmail())
                .build();
    }
}
