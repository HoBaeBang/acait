package com.aslan.academymanagement.config.auth;

import com.aslan.academymanagement.config.auth.dto.OAuthAttributes;
import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.Role;
import com.aslan.academymanagement.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // DB 조회 (자동 저장 X)
        Optional<Member> memberOptional = memberRepository.findByGoogleEmail(attributes.getEmail());

        // 권한 설정: DB에 있으면 해당 Role, 없으면 GUEST (임시 권한)
        // 주의: 여기서 GUEST를 준다고 로그인이 되는 건 아님. SuccessHandler에서 처리해야 함.
        String roleKey = memberOptional.map(Member::getRoleKey).orElse("ROLE_GUEST");

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(roleKey)),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }
}
