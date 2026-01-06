package com.aslan.academymanagement.config.auth.dto;

import com.aslan.academymanagement.domain.Member;
import com.aslan.academymanagement.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;
    private String providerId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture, String provider, String providerId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.provider = provider;
        this.providerId = providerId;
    }

    // OAuth2User에서 반환하는 사용자 정보는 Map이기 때문에 값 하나하나를 변환해야 함
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        // 여기서 registrationId(google, kakao, naver)를 체크해서 분기 처리 가능
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .provider("google")
                .providerId((String) attributes.get("sub")) // 구글의 PK는 sub
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // Member 엔티티로 변환 (가입 시점)
    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.GUEST) // 기본 권한 GUEST
                .provider(provider)
                .providerId(providerId)
                .build();
    }
}
