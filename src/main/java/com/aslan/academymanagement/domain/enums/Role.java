package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ROLE_ADMIN("ROLE_ADMIN", "원장"),
    ROLE_INSTRUCTOR("ROLE_INSTRUCTOR", "강사"),
    ROLE_GUEST("ROLE_GUEST", "손님"); // 임시 권한 추가

    private final String key;
    private final String title;
}
