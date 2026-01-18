package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ROLE_SUPER_ADMIN("ROLE_SUPER_ADMIN", "슈퍼 관리자"), // 개발자용
    ROLE_OWNER("ROLE_OWNER", "원장"),
    ROLE_INSTRUCTOR("ROLE_INSTRUCTOR", "강사"),
    ROLE_GUEST("ROLE_GUEST", "손님");

    private final String key;
    private final String title;
}
